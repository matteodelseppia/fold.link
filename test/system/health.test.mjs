// System test for the health API, run against a live app + Redis stack
// (see scripts/system-test.sh and docker-compose.system-test.yml). These
// are currently the only two APIs the application exposes.
import { test } from "node:test";
import assert from "node:assert/strict";

const BASE_URL = process.env.APP_BASE_URL ?? "http://localhost:8080";
const RETRY_ATTEMPTS = 10;
const RETRY_DELAY_MS = 500;

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// The Docker healthcheck that scripts/system-test.sh waits on only checks
// liveness (see Dockerfile), so readiness may still be catching up by the
// time the compose stack is reported healthy. A short retry absorbs that
// race without hiding a genuinely broken endpoint.
async function fetchHealthUntilUp(path) {
  let lastResponse;
  let lastBody;
  for (let attempt = 1; attempt <= RETRY_ATTEMPTS; attempt += 1) {
    const response = await fetch(new URL(path, BASE_URL));
    const body = await response.json();
    lastResponse = response;
    lastBody = body;
    if (response.status === 200 && body.status === "UP") {
      return { response, body };
    }
    if (attempt < RETRY_ATTEMPTS) {
      await sleep(RETRY_DELAY_MS);
    }
  }
  return { response: lastResponse, body: lastBody };
}

test("GET /actuator/health/liveness reports UP", async () => {
  const { response, body } = await fetchHealthUntilUp("/actuator/health/liveness");
  assert.equal(response.status, 200);
  assert.equal(body.status, "UP");
});

test("GET /actuator/health/readiness reports UP", async () => {
  const { response, body } = await fetchHealthUntilUp("/actuator/health/readiness");
  assert.equal(response.status, 200);
  assert.equal(body.status, "UP");
});
