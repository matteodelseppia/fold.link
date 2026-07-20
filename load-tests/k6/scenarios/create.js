// MVP-106: bounded k6 scenario for POST /api/v1/links.
//
// Runs on a single VU so alias uniqueness can be checked against every
// alias this run has actually seen (a plain in-VU Set). That is a check
// on the observed sample only, not a proof of global uniqueness - k6 VUs
// don't share memory, so a multi-VU run couldn't cross-check anyway. Each
// created destination carries a unique query value; each response is
// checked for status and the documented schema; each alias is verified
// to actually resolve straight after creation.
//
// Run directly:
//   BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/create.js
import { check } from "k6";
import { Trend, Rate } from "k6/metrics";
import { boundedDuration, boundedCreateIterations, thresholdsFor, randomDestination } from "../lib/config.js";
import { createLink, getAlias, isValidAlias } from "../lib/links.js";

const createDuration = new Trend("create_duration", true);
const createErrors = new Rate("create_errors");
const duplicateAliasRate = new Rate("duplicate_alias_rate");
const resolvesAfterCreate = new Rate("resolves_after_create");

const seenAliases = new Set();

export const options = {
  vus: 1,
  iterations: boundedCreateIterations("CREATE_ITERATIONS", 30),
  // iterations-based executors ignore `duration` as a stop condition, but
  // it still guards against a pathologically slow run hanging CI/staging.
  maxDuration: boundedDuration("DURATION", "60s"),
  thresholds: {
    ...thresholdsFor("create_duration", "http_req_failed", "checks"),
    duplicate_alias_rate: ["rate==0"],
    resolves_after_create: ["rate==1"],
  },
};

export default function () {
  const destination = randomDestination("create");
  const response = createLink(destination);

  createDuration.add(response.timings.duration);
  const created = response.status === 201 ? response.json() : undefined;

  const ok = check(response, {
    "create: status is 201": (r) => r.status === 201,
    "create: content-type is application/json": (r) => (r.headers["Content-Type"] || "").startsWith("application/json"),
    "create: alias matches the expected shape": () => created !== undefined && isValidAlias(created.alias),
    "create: destination echoes what was submitted": () => created !== undefined && created.destination === destination,
    "create: shortUrl ends with the alias": () => created !== undefined && created.shortUrl?.endsWith(`/${created.alias}`),
  });
  createErrors.add(!ok);

  if (created !== undefined && isValidAlias(created.alias)) {
    duplicateAliasRate.add(seenAliases.has(created.alias));
    seenAliases.add(created.alias);

    const redirectResponse = getAlias(created.alias);
    resolvesAfterCreate.add(redirectResponse.status === 302 && redirectResponse.headers["Location"] === destination);
  }
}
