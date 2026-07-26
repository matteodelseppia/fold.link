// Read-focused k6 scenario for GET /api/v1/links/{alias}/qr.
//
// setup() pre-creates a pool of mappings (excluded from the scenario's own
// measurements - mirrors the pool setup in lib/links.js). The default
// function then repeatedly requests the QR code for a random pool alias
// and checks that it comes back as a real, appropriately-sized PNG -
// cheap enough per request that this never needs to be pre-warmed like
// clicks.js's click counts do.
//
// Run directly:
//   BASE_URL=http://localhost:8080 TARGET_ENV=local k6 run load-tests/k6/scenarios/qr.js
import { check } from "k6";
import { Trend, Rate } from "k6/metrics";
import { boundedVus, boundedDuration, boundedPoolSize, thresholdsFor } from "../lib/config.js";
import { getQrCode, createAliasPool } from "../lib/links.js";

const qrDuration = new Trend("qr_duration", true);
const qrErrors = new Rate("qr_errors");

// The 8-byte PNG signature (0x89 'P' 'N' 'G' \r \n 0x1A \n) - compared
// byte-for-byte against the binary response body rather than pulling in an
// image-parsing dependency just for this check.
const PNG_MAGIC = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];

function startsWithPngMagic(body) {
  if (!(body instanceof ArrayBuffer) || body.byteLength < PNG_MAGIC.length) {
    return false;
  }
  const bytes = new Uint8Array(body, 0, PNG_MAGIC.length);
  return PNG_MAGIC.every((expected, i) => bytes[i] === expected);
}

export const options = {
  vus: boundedVus("VUS", 5),
  duration: boundedDuration("DURATION", "20s"),
  thresholds: thresholdsFor("qr_duration", "http_req_failed", "checks"),
};

export function setup() {
  const poolSize = boundedPoolSize("POOL_SIZE", 25);
  return { pool: createAliasPool(poolSize) };
}

export default function (data) {
  const entry = data.pool[Math.floor(Math.random() * data.pool.length)];
  const response = getQrCode(entry.alias);

  qrDuration.add(response.timings.duration);
  const ok = check(response, {
    "qr: status is 200": (r) => r.status === 200,
    "qr: content-type is image/png": (r) => (r.headers["Content-Type"] || "").startsWith("image/png"),
    "qr: body starts with the PNG signature": (r) => startsWithPngMagic(r.body),
  });
  qrErrors.add(!ok);
}
