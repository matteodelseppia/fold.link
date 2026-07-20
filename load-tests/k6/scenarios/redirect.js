// MVP-105: read-focused k6 scenario for GET /{alias}.
//
// setup() pre-creates a pool of mappings (excluded from redirect
// measurements - see lib/links.js). The default function then repeatedly
// requests a random alias from that pool, without following the
// redirect, and checks status/Location. Cycling through several aliases
// avoids a single-key artifact (e.g. one hot Redis key skewing results).
//
// Run directly:
//   BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/redirect.js
import { check } from "k6";
import { Trend, Rate } from "k6/metrics";
import { boundedVus, boundedDuration, boundedPoolSize, thresholdsFor } from "../lib/config.js";
import { getAlias, createAliasPool } from "../lib/links.js";

const redirectDuration = new Trend("redirect_duration", true);
const redirectErrors = new Rate("redirect_errors");

export const options = {
  vus: boundedVus("VUS", 5),
  duration: boundedDuration("DURATION", "20s"),
  thresholds: thresholdsFor("redirect_duration", "http_req_failed", "checks"),
};

export function setup() {
  const poolSize = boundedPoolSize("POOL_SIZE", 25);
  return { pool: createAliasPool(poolSize) };
}

export default function (data) {
  const entry = data.pool[Math.floor(Math.random() * data.pool.length)];
  const response = getAlias(entry.alias);

  redirectDuration.add(response.timings.duration);
  const ok = check(response, {
    "redirect: status is 302": (r) => r.status === 302,
    "redirect: Location matches the original destination": (r) => r.headers["Location"] === entry.destination,
  });
  redirectErrors.add(!ok);
}
