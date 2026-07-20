// MVP-107: read-heavy mixed workload (NF03) - substantially more
// redirects than creations, at a documented and reported ratio.
//
// This is the scenario run both as the CI smoke gate (MVP-109) and the
// pre-promotion staging gate (MVP-110): the redirect:create ratio
// (RATIO, default/floor 20:1) is enforced by config.js's safety bounds,
// and the actual counts are reported via the redirect_total /
// create_total Counters in k6's end-of-run summary. Redirect and creation
// each get their own latency/error metrics so a regression in one isn't
// masked by the other.
//
// Run directly:
//   BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/mixed.js
import { check } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";
import { boundedVus, boundedDuration, boundedPoolSize, boundedRatio, thresholdsFor, randomDestination } from "../lib/config.js";
import { createLink, getAlias, createAliasPool, isValidAlias } from "../lib/links.js";

const redirectDuration = new Trend("redirect_duration", true);
const redirectErrors = new Rate("redirect_errors");
const redirectTotal = new Counter("redirect_total");

const createDuration = new Trend("create_duration", true);
const createErrors = new Rate("create_errors");
const createTotal = new Counter("create_total");

const RATIO = boundedRatio("RATIO", 20);
// P(create) = 1 / (RATIO + 1) so that, in expectation, every create is
// accompanied by RATIO redirects - e.g. RATIO=20 -> ~4.76% creates.
const CREATE_PROBABILITY = 1 / (RATIO + 1);

export const options = {
  vus: boundedVus("VUS", 5),
  duration: boundedDuration("DURATION", "20s"),
  thresholds: thresholdsFor("redirect_duration", "create_duration", "http_req_failed", "checks"),
};

export function setup() {
  const poolSize = boundedPoolSize("POOL_SIZE", 25);
  return { pool: createAliasPool(poolSize), ratio: RATIO };
}

export default function (data) {
  if (Math.random() < CREATE_PROBABILITY) {
    doCreate();
  } else {
    doRedirect(data.pool);
  }
}

function doRedirect(pool) {
  const entry = pool[Math.floor(Math.random() * pool.length)];
  const response = getAlias(entry.alias);

  redirectDuration.add(response.timings.duration);
  redirectTotal.add(1);
  const ok = check(response, {
    "mixed/redirect: status is 302": (r) => r.status === 302,
    "mixed/redirect: Location matches the original destination": (r) => r.headers["Location"] === entry.destination,
  });
  redirectErrors.add(!ok);
}

function doCreate() {
  const destination = randomDestination("mixed");
  const response = createLink(destination);

  createDuration.add(response.timings.duration);
  createTotal.add(1);
  const created = response.status === 201 ? response.json() : undefined;
  const ok = check(response, {
    "mixed/create: status is 201": (r) => r.status === 201,
    "mixed/create: alias matches the expected shape": () => created !== undefined && isValidAlias(created.alias),
    "mixed/create: destination echoes what was submitted": () => created !== undefined && created.destination === destination,
  });
  createErrors.add(!ok);
}
