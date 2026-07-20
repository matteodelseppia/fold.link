#!/usr/bin/env bash
# CI k6 smoke test (MVP-109).
#
# Brings up the same disposable docker-compose.system-test.yml stack as
# scripts/system-test.sh (Redis + the given application image) in a
# uniquely-named Compose project, waits for both services to report
# healthy, then runs the k6 read-heavy mixed-workload scenario
# (load-tests/k6/scenarios/mixed.js) against it as a short smoke check.
# k6 exits nonzero on a threshold failure (e.g. elevated redirect error
# rate), which fails this script and blocks the pipeline.
#
# Usage:
#   scripts/k6-ci-smoke.sh <image-ref>
#
# Environment:
#   REDIS_IMAGE      Redis image to start (default: redis:7.4.2-alpine,
#                     matching scripts/system-test.sh).
#   APP_PORT          Host/container port the app listens on (default: 8080).
#   SUMMARY_PATH       Where to write the k6 JSON summary (default:
#                       smoke.k6-results.json, matching the *.k6-results.json
#                       pattern already in .gitignore), retained as a CI
#                       artifact by the caller.
#   K6_SMOKE_VUS, K6_SMOKE_DURATION, K6_SMOKE_POOL_SIZE, K6_SMOKE_RATIO
#                       Forwarded to the k6 scenario as VUS/DURATION/
#                       POOL_SIZE/RATIO (see
#                       load-tests/k6/scenarios/mixed.js) - all bounded by
#                       the safety limits in load-tests/k6/lib/config.js.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <image-ref>" >&2
  exit 1
fi

if ! command -v k6 >/dev/null 2>&1; then
  echo "k6 is required (pinned to v0.54.0 - see docs/milestones/mvp/toolchain.md) but was not found on PATH" >&2
  exit 1
fi

export APP_IMAGE="$1"
export REDIS_IMAGE="${REDIS_IMAGE:-redis:7.4.2-alpine}"
export APP_PORT="${APP_PORT:-8080}"
SUMMARY_PATH="${SUMMARY_PATH:-smoke.k6-results.json}"

RUN_ID="k6-smoke-$$-$(date +%s)"
COMPOSE=(docker compose -p "$RUN_ID" -f docker-compose.system-test.yml)

cleanup() {
  local exit_code=$?
  if [ "$exit_code" -ne 0 ]; then
    echo "--- k6 smoke test FAILED (run ${RUN_ID}); diagnostic logs follow ---" >&2
    "${COMPOSE[@]}" logs >&2 2>&1 || true
  fi
  echo "Cleaning up k6-smoke resources (run ${RUN_ID})..."
  "${COMPOSE[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
  exit "$exit_code"
}
trap cleanup EXIT

echo "Starting k6-smoke stack (run ${RUN_ID})..."
"${COMPOSE[@]}" up -d --wait --wait-timeout 90

echo "Running k6 mixed-workload smoke scenario against http://localhost:${APP_PORT}..."
BASE_URL="http://localhost:${APP_PORT}" \
  TARGET_ENV=ci \
  VUS="${K6_SMOKE_VUS:-5}" \
  DURATION="${K6_SMOKE_DURATION:-20s}" \
  POOL_SIZE="${K6_SMOKE_POOL_SIZE:-25}" \
  RATIO="${K6_SMOKE_RATIO:-20}" \
  k6 run --summary-export="$SUMMARY_PATH" load-tests/k6/scenarios/mixed.js

echo "k6 smoke test passed for image: ${APP_IMAGE}"
