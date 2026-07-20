// Unit tests for the static frontend (src/main/resources/static/) using
// jsdom - no browser, no build step, matching the plain <script> the app
// itself ships. Loads the real index.html and js/app.js from disk so the
// tests exercise exactly what's served, not a copy.
import { test } from "node:test";
import assert from "node:assert/strict";
import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { JSDOM } from "jsdom";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const STATIC_DIR = path.resolve(__dirname, "../../src/main/resources/static");
const HTML = fs.readFileSync(path.join(STATIC_DIR, "index.html"), "utf8");
const APP_SCRIPT = fs.readFileSync(path.join(STATIC_DIR, "js/app.js"), "utf8");

function buildDom({ fetchImpl, clipboardImpl } = {}) {
  const dom = new JSDOM(HTML, { runScripts: "outside-only", url: "http://localhost/" });
  const { window } = dom;

  window.fetch =
    fetchImpl ??
    (async () => {
      throw new Error("fetch was not expected to be called");
    });

  Object.defineProperty(window.navigator, "clipboard", {
    value: clipboardImpl ?? { writeText: async () => {} },
    configurable: true,
  });

  window.eval(APP_SCRIPT);
  return dom;
}

function elements(dom) {
  const { document } = dom.window;
  return {
    document,
    form: document.getElementById("shorten-form"),
    input: document.getElementById("url-input"),
    submitBtn: document.getElementById("submit-btn"),
    formError: document.getElementById("form-error"),
    result: document.getElementById("result"),
    resultLink: document.getElementById("result-link"),
    copyBtn: document.getElementById("copy-btn"),
    copyStatus: document.getElementById("copy-status"),
  };
}

function submitForm(dom, url) {
  const { form, input } = elements(dom);
  input.value = url;
  form.dispatchEvent(new dom.window.Event("submit", { cancelable: true, bubbles: true }));
}

async function flush() {
  await new Promise((resolve) => setTimeout(resolve, 0));
  await new Promise((resolve) => setTimeout(resolve, 0));
}

function jsonResponse(status, body) {
  return {
    ok: status >= 200 && status < 300,
    status,
    json: async () => body,
  };
}

// --- MVP-082: HTML shell -----------------------------------------------

test("the page has a labeled input with help text and result/error landmarks", () => {
  const dom = buildDom();
  const { document, input, formError, result } = elements(dom);

  const label = document.querySelector('label[for="url-input"]');
  assert.ok(label, "expected a <label for=url-input>");
  assert.equal(input.getAttribute("aria-describedby"), "url-help");
  assert.ok(document.getElementById("url-help"), "expected the referenced help text element");

  assert.equal(formError.getAttribute("role"), "alert");
  assert.equal(formError.hidden, true);
  assert.equal(result.hidden, true);
});

// --- MVP-085: client-side validation -------------------------------------

test("blank input shows an error and never calls fetch", async () => {
  const dom = buildDom();
  const { formError, input } = elements(dom);

  submitForm(dom, "   ");
  await flush();

  assert.equal(formError.hidden, false);
  assert.match(formError.textContent, /enter a url/i);
  assert.equal(input.getAttribute("aria-invalid"), "true");
});

test("an unsupported scheme shows an error and never calls fetch", async () => {
  const dom = buildDom();
  const { formError } = elements(dom);

  submitForm(dom, "ftp://example.com/file");
  await flush();

  assert.equal(formError.hidden, false);
  assert.match(formError.textContent, /http:\/\/ or https:\/\//);
});

test("http and https destinations both pass client validation through to fetch", async () => {
  let calls = 0;
  const dom = buildDom({
    fetchImpl: async () => {
      calls += 1;
      return jsonResponse(201, { alias: "abc12345", shortUrl: "http://x/abc12345", destination: "" });
    },
  });

  submitForm(dom, "http://example.com");
  await flush();
  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(calls, 2);
});

// --- MVP-084: submit flow -------------------------------------------------

test("submits the exact JSON contract to POST /api/v1/links", async () => {
  let request;
  const dom = buildDom({
    fetchImpl: async (url, init) => {
      request = { url, init };
      return jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com/page",
      });
    },
  });

  submitForm(dom, "https://example.com/page");
  await flush();

  assert.equal(request.url, "/api/v1/links");
  assert.equal(request.init.method, "POST");
  assert.equal(request.init.headers["Content-Type"], "application/json");
  assert.deepEqual(JSON.parse(request.init.body), { url: "https://example.com/page" });
});

test("the submit button is disabled while a request is pending and re-enabled after", async () => {
  let resolveFetch;
  const dom = buildDom({
    fetchImpl: () =>
      new Promise((resolve) => {
        resolveFetch = () =>
          resolve(
            jsonResponse(201, {
              alias: "abc12345",
              shortUrl: "http://localhost/abc12345",
              destination: "https://example.com",
            }),
          );
      }),
  });
  const { submitBtn } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  assert.equal(submitBtn.disabled, true);
  assert.match(submitBtn.textContent, /shortening/i);

  resolveFetch();
  await flush();
  assert.equal(submitBtn.disabled, false);
  assert.equal(submitBtn.querySelector(".btn-label").textContent, "Shorten");
});

test("a second submission while one is pending does not send a second request", async () => {
  let calls = 0;
  let resolveFetch;
  const dom = buildDom({
    fetchImpl: () => {
      calls += 1;
      return new Promise((resolve) => {
        resolveFetch = () =>
          resolve(
            jsonResponse(201, {
              alias: "abc12345",
              shortUrl: "http://localhost/abc12345",
              destination: "https://example.com",
            }),
          );
      });
    },
  });

  submitForm(dom, "https://example.com");
  await flush();
  submitForm(dom, "https://example.com/second");
  await flush();

  assert.equal(calls, 1);
  resolveFetch();
  await flush();
});

// --- MVP-086: rendering the created short link ---------------------------

test("renders the alias/shortUrl exactly as returned, as a real link", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com/page",
      }),
  });
  const { result, resultLink } = elements(dom);

  submitForm(dom, "https://example.com/page");
  await flush();

  assert.equal(result.hidden, false);
  assert.equal(resultLink.textContent, "http://localhost/abc12345");
  assert.equal(resultLink.getAttribute("href"), "http://localhost/abc12345");
});

test("a hostile shortUrl string is rendered as inert text, never parsed as HTML", async () => {
  const hostile = "http://localhost/<img src=x onerror=alert(1)>";
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, { alias: "x", shortUrl: hostile, destination: "https://example.com" }),
  });
  const { resultLink } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(resultLink.textContent, hostile);
  assert.equal(resultLink.querySelector("img"), null);
});

test("a new successful submission replaces stale result content", async () => {
  let response = jsonResponse(201, {
    alias: "first111",
    shortUrl: "http://localhost/first111",
    destination: "https://example.com/one",
  });
  const dom = buildDom({ fetchImpl: async () => response });
  const { resultLink } = elements(dom);

  submitForm(dom, "https://example.com/one");
  await flush();
  assert.equal(resultLink.textContent, "http://localhost/first111");

  response = jsonResponse(201, {
    alias: "second22",
    shortUrl: "http://localhost/second22",
    destination: "https://example.com/two",
  });
  submitForm(dom, "https://example.com/two");
  await flush();
  assert.equal(resultLink.textContent, "http://localhost/second22");
});

// --- MVP-087: copy-to-clipboard -------------------------------------------

test("the copy button is disabled until a short URL exists", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { copyBtn } = elements(dom);

  assert.equal(copyBtn.disabled, true);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(copyBtn.disabled, false);
});

test("a successful copy announces success without moving focus", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
    clipboardImpl: { writeText: async () => {} },
  });
  const { copyBtn, copyStatus, document } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  copyBtn.focus();
  copyBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await flush();

  assert.match(copyStatus.textContent, /copied/i);
  assert.equal(copyStatus.getAttribute("aria-live"), "polite");
  assert.equal(document.activeElement, copyBtn);
});

test("a rejected copy keeps the link selectable and shows fallback guidance", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
    clipboardImpl: {
      writeText: async () => {
        throw new Error("denied");
      },
    },
  });
  const { copyBtn, copyStatus, resultLink } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  copyBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await flush();

  assert.match(copyStatus.textContent, /select the link/i);
  assert.equal(resultLink.getAttribute("href"), "http://localhost/abc12345");
});

// --- MVP-088: error handling -----------------------------------------------

test("a 400 VALIDATION_ERROR shows the server's own message", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(400, { error: "VALIDATION_ERROR", message: "url must have a valid host" }),
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(formError.textContent, "url must have a valid host");
});

test("a 503 STORAGE_ERROR shows a retry-friendly message, not raw server detail", async () => {
  const dom = buildDom({
    fetchImpl: async () => jsonResponse(503, { error: "STORAGE_ERROR", message: "redis unreachable" }),
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.match(formError.textContent, /temporarily unavailable/i);
  assert.doesNotMatch(formError.textContent, /redis/i);
});

test("a 500 with an unrecognized error code shows a retry-friendly message, not the raw code", async () => {
  const dom = buildDom({
    fetchImpl: async () => jsonResponse(500, { error: "INTERNAL_ERROR", message: "stack trace leak" }),
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(formError.hidden, false);
  assert.doesNotMatch(formError.textContent, /stack trace/i);
  assert.doesNotMatch(formError.textContent, /INTERNAL_ERROR/);
});

test("a 4xx with an unrecognized error code shows a generic fallback message", async () => {
  const dom = buildDom({
    fetchImpl: async () => jsonResponse(409, { error: "SOME_UNKNOWN_CODE", message: "internal detail" }),
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.match(formError.textContent, /something went wrong/i);
  assert.doesNotMatch(formError.textContent, /internal detail/i);
});

test("an invalid JSON response body still shows a safe fallback message", async () => {
  const dom = buildDom({
    fetchImpl: async () => ({
      ok: false,
      status: 502,
      json: async () => {
        throw new SyntaxError("Unexpected token");
      },
    }),
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(formError.hidden, false);
  assert.ok(formError.textContent.length > 0);
});

test("a network error (rejected fetch) explains the user may retry", async () => {
  const dom = buildDom({
    fetchImpl: async () => {
      throw new TypeError("Failed to fetch");
    },
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.match(formError.textContent, /network error/i);
});

test("a failed request clears stale success content and permits a retry that succeeds", async () => {
  let shouldFail = true;
  const dom = buildDom({
    fetchImpl: async () => {
      if (shouldFail) {
        return jsonResponse(503, { error: "STORAGE_ERROR", message: "down" });
      }
      return jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      });
    },
  });
  const { result, resultLink, formError } = elements(dom);

  submitForm(dom, "https://example.com/one");
  await flush();
  assert.equal(formError.hidden, false);
  assert.equal(result.hidden, true);

  shouldFail = false;
  submitForm(dom, "https://example.com/two");
  await flush();

  assert.equal(formError.hidden, true);
  assert.equal(result.hidden, false);
  assert.equal(resultLink.textContent, "http://localhost/abc12345");
});

test("a success clears a previously shown error", async () => {
  let shouldFail = true;
  const dom = buildDom({
    fetchImpl: async () => {
      if (shouldFail) {
        return jsonResponse(400, { error: "VALIDATION_ERROR", message: "bad url" });
      }
      return jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      });
    },
  });
  const { formError } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  assert.equal(formError.hidden, false);

  shouldFail = false;
  submitForm(dom, "https://example.com");
  await flush();
  assert.equal(formError.hidden, true);
});

// --- MVP-089: keyboard/focus behavior --------------------------------------

test("client and server validation errors move focus to the input", async () => {
  const dom = buildDom({
    fetchImpl: async () => jsonResponse(400, { error: "VALIDATION_ERROR", message: "bad" }),
  });
  const { input, document } = elements(dom);

  submitForm(dom, "not-a-url");
  await flush();
  assert.equal(document.activeElement, input);

  submitForm(dom, "https://example.com");
  await flush();
  assert.equal(document.activeElement, input);
});

test("tab order follows input, submit button, result link, then copy button", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { document, input, submitBtn, resultLink, copyBtn } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  const focusable = Array.from(
    document.querySelectorAll("input, button, a[href]"),
  ).filter((el) => !el.hasAttribute("disabled") && !el.closest("[hidden]"));

  assert.deepEqual(focusable, [input, submitBtn, resultLink, copyBtn]);
});
