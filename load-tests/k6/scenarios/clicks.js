// Read-focused k6 scenario for GET /api/v1/links/{alias}/clicks.
//
// setup() pre-creates a pool of mappings and, for each, issues a handful
// of redirects up front so the click counter has a known, non-zero value
// to read back (excluded from the scenario's own measurements - mirrors
// the redirect pool setup in lib/links.js). The default function then
// repeatedly reads the click count for a random pool alias and checks
// that it never drops below the count observed at setup time (it can
// only grow, since nothing else redirects through these aliases during
// the run).
//
// Run directly:
//   BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/clicks.js
import { check } from "k6";
import { Trend, Rate } from "k6/metrics";
import { boundedVus, boundedDuration, boundedPoolSize, thresholdsFor } from "../lib/config.js";
import { getAlias, getClickCount, createAliasPool } from "../lib/links.js";

const clickCountDuration = new Trend("click_count_duration", true);
const clickCountErrors = new Rate("click_count_errors");

const CLICKS_PER_ALIAS = 3;

export const options = {
  vus: boundedVus("VUS", 5),
  duration: boundedDuration("DURATION", "20s"),
  thresholds: thresholdsFor("click_count_duration", "http_req_failed", "checks"),
};

export function setup() {
  const poolSize = boundedPoolSize("POOL_SIZE", 25);
  const pool = createAliasPool(poolSize);
  for (const entry of pool) {
    for (let i = 0; i < CLICKS_PER_ALIAS; i++) {
      getAlias(entry.alias);
    }
  }
  return { pool };
}

export default function (data) {
  const entry = data.pool[Math.floor(Math.random() * data.pool.length)];
  const response = getClickCount(entry.alias);

  clickCountDuration.add(response.timings.duration);
  const ok = check(response, {
    "click count: status is 200": (r) => r.status === 200,
    "click count: is at least the pre-seeded count": (r) => Number(r.body) >= CLICKS_PER_ALIAS,
  });
  clickCountErrors.add(!ok);
}
