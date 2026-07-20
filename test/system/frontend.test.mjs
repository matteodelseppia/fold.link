// System test for the static frontend (src/main/resources/static/), run
// against a live app + Redis stack (see scripts/system-test.sh and
// docker-compose.system-test.yml). Traces to F04 (web UI to submit long
// URLs and copy the resulting short URL) - the interactive submit/copy
// behavior itself is unit-tested with jsdom in test/frontend/app.test.mjs,
// since that needs DOM/fetch/clipboard mocking rather than a live network.
import { test } from "node:test";
import assert from "node:assert/strict";

const BASE_URL = process.env.APP_BASE_URL ?? "http://localhost:8080";

test("F04: GET / serves the frontend page with the expected form", async () => {
  const response = await fetch(new URL("/", BASE_URL));
  const body = await response.text();

  assert.equal(response.status, 200);
  assert.equal(response.headers.get("content-type")?.split(";")[0], "text/html");
  assert.match(body, /id="shorten-form"/);
  assert.match(body, /id="url-input"/);
});

test("F04: static assets referenced by the page are actually served", async () => {
  const cssResponse = await fetch(new URL("/css/styles.css", BASE_URL));
  const jsResponse = await fetch(new URL("/js/app.js", BASE_URL));

  assert.equal(cssResponse.status, 200);
  assert.equal(cssResponse.headers.get("content-type")?.split(";")[0], "text/css");

  assert.equal(jsResponse.status, 200);
  assert.match(jsResponse.headers.get("content-type") ?? "", /javascript/);
});

test("the frontend page and the link APIs do not collide on routing", async () => {
  const pageResponse = await fetch(new URL("/", BASE_URL));
  const apiResponse = await fetch(new URL("/api/v1/links", BASE_URL), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url: "https://example.com/frontend-routing-check" }),
  });

  assert.equal(pageResponse.status, 200);
  assert.equal(apiResponse.status, 201);
});
