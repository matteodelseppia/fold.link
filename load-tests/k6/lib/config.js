// Shared k6 configuration: target resolution with production-safety
// guards, environment-aware safety bounds for duration/VUs/pool sizes,
// environment-aware thresholds, and small test-data helpers.
//
// See docs/milestones/mvp/load-testing.md for the threshold rationale,
// the safety-bound rationale, and the revision process (MVP-104,
// MVP-108).
//
// All checks in this module run at script-init time (module load), so an
// invalid or absent target fails before any VU starts and before any
// traffic is sent - see MVP-104's testing criterion.

const LOOPBACK_HOSTS = new Set(["localhost", "127.0.0.1", "0.0.0.0", "[::1]", "host.docker.internal"]);

function fail(message) {
  throw new Error(`[k6 config] ${message}`);
}

// k6's JS runtime has no built-in URL global (unlike Node/browsers), so
// this is a minimal regex-based hostname extractor rather than
// `new URL(...).hostname` - good enough for the absolute http(s) URLs
// these scripts are ever pointed at.
function parseHost(rawUrl, label) {
  const match = /^[a-zA-Z][a-zA-Z0-9+.-]*:\/\/(\[[^\]]+\]|[^/:?#]+)(?::\d+)?/.exec(rawUrl.trim());
  if (!match) {
    fail(`${label} "${rawUrl}" is not a valid absolute URL (expected e.g. "http://host:port")`);
  }
  return match[1].toLowerCase();
}

export const TARGET_ENV = (__ENV.TARGET_ENV || "local").toLowerCase();
if (!["local", "ci", "staging"].includes(TARGET_ENV)) {
  fail(
    `TARGET_ENV must be one of "local", "ci", "staging" (got "${TARGET_ENV}"). ` +
      "Production is never a valid target for these scripts.",
  );
}

const rawBaseUrl = __ENV.BASE_URL || (TARGET_ENV === "local" ? "http://localhost:8080" : "");
if (!rawBaseUrl) {
  fail(`BASE_URL is required for TARGET_ENV="${TARGET_ENV}" (no default is provided for non-local targets).`);
}

const targetHost = parseHost(rawBaseUrl, "BASE_URL");
const isLoopback = LOOPBACK_HOSTS.has(targetHost);

if ((TARGET_ENV === "local" || TARGET_ENV === "ci") && !isLoopback) {
  fail(
    `TARGET_ENV="${TARGET_ENV}" requires a loopback BASE_URL (got host "${targetHost}"). ` +
      "This guards against a local or CI run accidentally pointing at a real deployment.",
  );
}

if (TARGET_ENV === "staging") {
  if (isLoopback) {
    fail(`TARGET_ENV="staging" requires a real staging BASE_URL, not a loopback host ("${targetHost}").`);
  }
  if (__ENV.CONFIRM_STAGING_TARGET !== "yes") {
    fail(
      'TARGET_ENV="staging" requires CONFIRM_STAGING_TARGET=yes as an explicit opt-in, ' +
        "so an unattended or copy-pasted invocation can't silently send load to a real environment.",
    );
  }
  const productionHint = __ENV.PRODUCTION_BASE_URL_HINT;
  if (productionHint) {
    const productionHost = parseHost(productionHint, "PRODUCTION_BASE_URL_HINT");
    if (productionHost === targetHost) {
      fail(`BASE_URL host "${targetHost}" matches PRODUCTION_BASE_URL_HINT - refusing to load-test production.`);
    }
  }
}

export const BASE_URL = rawBaseUrl.replace(/\/+$/, "");

// --- Safety bounds -----------------------------------------------------
//
// Every tunable knob is overrideable via environment variable, but only
// within these documented bounds - out-of-range values fail fast instead
// of silently clamping, so a mistyped override can't quietly send more
// load than intended.

const BOUNDS = {
  vus: { local: [1, 50], ci: [1, 50], staging: [1, 15] },
  durationSeconds: { local: [5, 120], ci: [5, 120], staging: [10, 90] },
  poolSize: { local: [5, 200], ci: [5, 200], staging: [5, 60] },
  createIterations: { local: [1, 200], ci: [1, 200], staging: [1, 50] },
  redirectToCreateRatio: { local: [20, 500], ci: [20, 500], staging: [20, 500] },
};

function parseDurationSeconds(value, envVarName) {
  const match = /^(\d+(?:\.\d+)?)(ms|s|m|h)$/.exec(value.trim());
  if (!match) {
    fail(`${envVarName}="${value}" is not a supported k6 duration (expected e.g. "20s" or "2m")`);
  }
  const amount = Number(match[1]);
  const unitSeconds = { ms: 0.001, s: 1, m: 60, h: 3600 }[match[2]];
  return amount * unitSeconds;
}

function boundedNumber(envVarName, defaultValue, boundsKey) {
  const [min, max] = BOUNDS[boundsKey][TARGET_ENV];
  const raw = __ENV[envVarName];
  if (raw === undefined || raw === "") return defaultValue;
  const value = Number(raw);
  if (!Number.isFinite(value)) fail(`${envVarName} must be a number (got "${raw}")`);
  if (value < min || value > max) {
    fail(`${envVarName}=${value} is outside the permitted safety bounds [${min}, ${max}] for TARGET_ENV="${TARGET_ENV}"`);
  }
  return value;
}

export function boundedVus(envVarName, defaultValue) {
  return boundedNumber(envVarName, defaultValue, "vus");
}

export function boundedDuration(envVarName, defaultValue) {
  const raw = __ENV[envVarName] || defaultValue;
  const seconds = parseDurationSeconds(raw, envVarName);
  const [min, max] = BOUNDS.durationSeconds[TARGET_ENV];
  if (seconds < min || seconds > max) {
    fail(`${envVarName}="${raw}" (${seconds}s) is outside the permitted safety bounds [${min}s, ${max}s] for TARGET_ENV="${TARGET_ENV}"`);
  }
  return raw;
}

export function boundedPoolSize(envVarName, defaultValue) {
  return boundedNumber(envVarName, defaultValue, "poolSize");
}

export function boundedCreateIterations(envVarName, defaultValue) {
  return boundedNumber(envVarName, defaultValue, "createIterations");
}

export function boundedRatio(envVarName, defaultValue) {
  return boundedNumber(envVarName, defaultValue, "redirectToCreateRatio");
}

// --- Thresholds (MVP-108) -----------------------------------------------
//
// Pragmatic pass/fail thresholds for the bounded MVP workload (NF03).
// Staging thresholds are looser than CI's to account for real network
// latency and a shared Railway host rather than a container on the same
// runner. Redirect and creation are evaluated as separate metrics because
// NF03 treats them as different workloads with different cost profiles.
// See docs/milestones/mvp/load-testing.md for the rationale and the
// process for revising these numbers.

const THRESHOLDS_BY_ENV = {
  local: {
    redirect_duration: ["p(95)<300", "p(99)<800"],
    create_duration: ["p(95)<500", "p(99)<1200"],
    http_req_failed: ["rate<0.01"],
    checks: ["rate>0.99"],
  },
  ci: {
    redirect_duration: ["p(95)<300", "p(99)<800"],
    create_duration: ["p(95)<500", "p(99)<1200"],
    http_req_failed: ["rate<0.01"],
    checks: ["rate>0.99"],
  },
  staging: {
    redirect_duration: ["p(95)<500", "p(99)<1200"],
    create_duration: ["p(95)<800", "p(99)<1800"],
    http_req_failed: ["rate<0.02"],
    checks: ["rate>0.98"],
  },
};

export function thresholdsFor(...metricNames) {
  const all = THRESHOLDS_BY_ENV[TARGET_ENV];
  const selected = {};
  for (const name of metricNames) {
    if (all[name]) selected[name] = all[name];
  }
  return selected;
}

// --- Test data helpers ---------------------------------------------------

const ALIAS_LENGTH = Number(__ENV.ALIAS_LENGTH || 8);
export const ALIAS_PATTERN = new RegExp(`^[A-Za-z0-9_-]{${ALIAS_LENGTH}}$`);

// A safe, non-production destination host that doesn't need to resolve -
// the app only stores and echoes it back, it never fetches it. Avoids
// __VU/__ITER since this also needs to work from setup(), where neither
// is defined.
export function randomDestination(prefix = "loadtest") {
  const unique = `${Date.now()}-${Math.floor(Math.random() * 1e12)}`;
  return `https://example.com/${prefix}/${unique}?probe=${unique}`;
}
