# fold.link - k6 Load Testing

Covers MVP-104 through MVP-110: the k6 configuration, scenarios, safety
guards, thresholds, and where they run in CI and post-deploy.

## Where things live

- `load-tests/k6/lib/config.js` - target resolution, production-safety
  guards, safety bounds, thresholds, test-data helpers (MVP-104, MVP-108)
- `load-tests/k6/lib/links.js` - thin API helpers (create/get) shared by
  the scenarios
- `load-tests/k6/scenarios/redirect.js` - read-focused `GET /{alias}`
  scenario (MVP-105)
- `load-tests/k6/scenarios/create.js` - bounded `POST /api/v1/links`
  scenario (MVP-106)
- `load-tests/k6/scenarios/mixed.js` - read-heavy mixed NF03 workload
  (MVP-107) - the scenario run in CI and against staging
- `scripts/k6-ci-smoke.sh` - brings up the disposable CI stack and runs
  the mixed scenario as a smoke test (MVP-109)

k6 itself is pinned to **v0.54.0** - see
[toolchain.md](toolchain.md#k6) for install instructions.

## Production safety

`load-tests/k6/lib/config.js` resolves `TARGET_ENV` (`local` | `ci` |
`staging`) and `BASE_URL` at script-init time, before any VU starts:

- `TARGET_ENV` must be one of the three values above - there is no
  `production` code path in these scripts at all.
- `local`/`ci` require a loopback `BASE_URL` (default
  `http://localhost:8080` for `local`; `ci` has no default and is always
  set explicitly by `scripts/k6-ci-smoke.sh`).
- `staging` requires a non-loopback `BASE_URL`, an explicit
  `CONFIRM_STAGING_TARGET=yes` opt-in, and (in CI) a
  `PRODUCTION_BASE_URL_HINT` the script cross-checks `BASE_URL` against
  and refuses to run if they match.

An invalid or absent target throws during module init, so no HTTP request
is ever sent.

## Safety bounds

`VUS`, `DURATION`, `POOL_SIZE`, `CREATE_ITERATIONS`, and `RATIO` are all
overrideable via environment variable, but only within the bounds in
`config.js`'s `BOUNDS` table (tighter for `staging` than `local`/`ci`, to
respect Railway's plan limits). An out-of-range override fails fast
rather than silently clamping.

## Thresholds (MVP-108)

- `redirect_duration` - CI/local: p95 < 300ms, p99 < 800ms; staging: p95 <
  500ms, p99 < 1200ms
- `create_duration` - CI/local: p95 < 500ms, p99 < 1200ms; staging: p95 <
  800ms, p99 < 1800ms
- `http_req_failed` - CI/local: rate < 1%; staging: rate < 2%
- `checks` - CI/local: rate > 99%; staging: rate > 98%

Redirect and creation are evaluated as separate metrics, matching NF03's
framing of them as different workloads (read-heavy vs. occasional write)
with different cost profiles.

**Rationale**: these are pragmatic MVP starting points, not derived from a
capacity model - CI numbers assume a container on the same GitHub-hosted
runner (near-zero network latency), staging numbers add headroom for real
network latency to Railway and a shared host. They exist to catch gross
regressions (a broken index, an N+1 lookup, a blocking call on the
redirect path), not to certify a specific production SLA.

**Revising thresholds**: change the relevant entry in
`THRESHOLDS_BY_ENV` in `load-tests/k6/lib/config.js` and update the table
above in the same change, with a one-line reason (e.g. "loosened staging
create p99 after observing X"). Treat a threshold loosening as a change
that needs the same review scrutiny as the code it's gating - it's easy to
silently erode the gate by loosening it whenever it's inconvenient.

## Where these run

- **CI (`k6_smoke` job in `.github/workflows/ci.yml`)**: runs after
  `system_test`, via `scripts/k6-ci-smoke.sh`, against the same disposable
  `docker-compose.system-test.yml` stack. Short (default 20s, 5 VUs)
  mixed-workload smoke run; a threshold failure fails the job and blocks
  merge. The k6 JSON summary is retained as the `k6-smoke-summary`
  artifact.
- **Post-deploy staging gate (`k6_staging_gate` job in
  `.github/workflows/post-deploy-system-tests.yml`)**: runs after the
  functional staging system tests pass, against the real
  `secrets.STAGING_BASE_URL`, with `CONFIRM_STAGING_TARGET=yes` and
  `PRODUCTION_BASE_URL_HINT` set from `vars.PRODUCTION_BASE_URL`. Slightly
  longer/larger than the CI smoke run (30s, 5 VUs) but still bounded. The
  summary is retained as an artifact named with the deployed commit SHA.

## Running locally

```bash
# against a locally running app (e.g. ./gradlew bootRun / docker compose up)
BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/mixed.js
BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/redirect.js
BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/create.js

# the same disposable stack CI uses, end-to-end
docker build -t foldlink-app:local .
./scripts/k6-ci-smoke.sh foldlink-app:local
```

### Testing evidence

- **One-iteration local smoke run passes** (MVP-104): `CREATE_ITERATIONS=1
  BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run
  load-tests/k6/scenarios/create.js` against a locally running app exits
  0 with zero check failures.
- **Absent target fails safely** (MVP-104): `unset BASE_URL;
  TARGET_ENV=staging k6 run load-tests/k6/scenarios/redirect.js` exits
  nonzero immediately with `BASE_URL is required for
  TARGET_ENV="staging"` and no HTTP request is logged.
- **Redirect scenario** (MVP-105): a local run against a running app shows
  nonzero `redirect_duration` samples and `checks: 100.00%` in the k6
  summary.
- **Create scenario** (MVP-106): a local run followed by manually
  requesting each reported alias confirms every one resolves (also
  asserted in-script via the `resolves_after_create` metric/threshold).
- **Mixed scenario ratio** (MVP-107): a local run's summary reports
  `redirect_total` and `create_total`; dividing them lands within a couple
  of points of the configured `RATIO` (probabilistic selection, not exact
  per-run).
- **Thresholds** (MVP-108): a healthy local run passes; temporarily
  setting an impossible threshold (e.g. editing `redirect_duration` to
  `["p(95)<1"]` in `config.js`) makes the same run exit nonzero.
- **CI smoke gate** (MVP-109): a deliberate redirect assertion break (e.g.
  temporarily checking the wrong `Location`) or an impossible threshold
  fails the `k6_smoke` job.
- **Staging gate** (MVP-110): one full run of `k6_staging_gate` against a
  real staging deployment passes, with the `k6-staging-summary-<sha>`
  artifact retained.
