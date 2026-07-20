// System test for the link-creation and redirect APIs, run against a live
// app + Redis stack (see scripts/system-test.sh and
// docker-compose.system-test.yml). Covers the golden path plus the edge
// and failure cases from the ADR-001 contract: validation errors and
// unknown/invalid aliases. Storage-unavailable (503) behavior is exercised
// in the Java repository/controller tests instead - it needs Redis to be
// down while the app is up, which this disposable stack (Redis marked
// service_healthy before the app starts) doesn't produce.
import { test } from "node:test";
import assert from "node:assert/strict";
import crypto from "node:crypto";

const BASE_URL = process.env.APP_BASE_URL ?? "http://localhost:8080";

async function createLink(url) {
  const response = await fetch(new URL("/api/v1/links", BASE_URL), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ url }),
  });
  const body = await response.json();
  return { response, body };
}

test("F01/F04: POST /api/v1/links creates a link for a valid destination", async () => {
  const destination = `https://example.com/${crypto.randomUUID()}`;
  const { response, body } = await createLink(destination);

  assert.equal(response.status, 201);
  assert.equal(response.headers.get("content-type")?.split(";")[0], "application/json");
  assert.match(body.alias, /^[A-Za-z0-9_-]{8}$/);
  assert.equal(body.destination, destination);
  assert.equal(body.shortUrl, `${BASE_URL}/${body.alias}`);
});

test("F02: GET /{alias} redirects to the original destination", async () => {
  const destination = `https://example.com/redirect-target/${crypto.randomUUID()}`;
  const { body: created } = await createLink(destination);

  const response = await fetch(new URL(`/${created.alias}`, BASE_URL), {
    redirect: "manual",
  });

  assert.equal(response.status, 302);
  assert.equal(response.headers.get("location"), destination);
});

test("F02: redirect preserves query strings and fragments in the destination", async () => {
  const destination = `https://example.com/path?x=1&y=two#section-${crypto.randomUUID()}`;
  const { body: created } = await createLink(destination);

  const response = await fetch(new URL(`/${created.alias}`, BASE_URL), {
    redirect: "manual",
  });

  assert.equal(response.status, 302);
  assert.equal(response.headers.get("location"), destination);
});

test("F03: GET /{alias} returns 404 for an alias that was never created", async () => {
  const response = await fetch(new URL("/aaaaaaaa", BASE_URL), { redirect: "manual" });
  const body = await response.json();

  assert.equal(response.status, 404);
  assert.equal(body.error, "ALIAS_NOT_FOUND");
});

test("F03: GET /{alias} returns 404 for a syntactically invalid alias", async () => {
  const response = await fetch(new URL("/too-short", BASE_URL), { redirect: "manual" });

  assert.equal(response.status, 404);
});

test("F05: POST /api/v1/links rejects a blank url with 400", async () => {
  const { response, body } = await createLink("   ");

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("F05: POST /api/v1/links rejects an unsupported scheme with 400", async () => {
  const { response, body } = await createLink("javascript:alert(1)");

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("F05: POST /api/v1/links rejects a relative url with 400", async () => {
  const { response, body } = await createLink("/just/a/path");

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("F05: POST /api/v1/links rejects a hostless url with 400", async () => {
  const { response, body } = await createLink("http:///path");

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("POST /api/v1/links rejects a missing url field with 400", async () => {
  const response = await fetch(new URL("/api/v1/links", BASE_URL), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({}),
  });
  const body = await response.json();

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("POST /api/v1/links rejects malformed JSON with 400", async () => {
  const response = await fetch(new URL("/api/v1/links", BASE_URL), {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: "{not-json",
  });
  const body = await response.json();

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("POST /api/v1/links rejects an unsupported content type with 415", async () => {
  const response = await fetch(new URL("/api/v1/links", BASE_URL), {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: "https://example.com",
  });

  assert.equal(response.status, 415);
});

test("POST /api/v1/links rejects an oversized destination with 400", async () => {
  const oversized = `https://example.com/${"a".repeat(2100)}`;
  const { response, body } = await createLink(oversized);

  assert.equal(response.status, 400);
  assert.equal(body.error, "VALIDATION_ERROR");
});

test("repeated creation of the same destination yields independent, working aliases", async () => {
  const destination = `https://example.com/shared/${crypto.randomUUID()}`;
  const first = await createLink(destination);
  const second = await createLink(destination);

  assert.equal(first.response.status, 201);
  assert.equal(second.response.status, 201);
  assert.notEqual(first.body.alias, second.body.alias);

  for (const created of [first.body, second.body]) {
    const redirectResponse = await fetch(new URL(`/${created.alias}`, BASE_URL), {
      redirect: "manual",
    });
    assert.equal(redirectResponse.status, 302);
    assert.equal(redirectResponse.headers.get("location"), destination);
  }
});
