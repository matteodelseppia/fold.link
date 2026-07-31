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
const CSS = fs.readFileSync(path.join(STATIC_DIR, "css/styles.css"), "utf8");
const HTML_WITH_INLINE_CSS = HTML.replace(
  '<link rel="stylesheet" href="/css/styles.css" />',
  `<style>${CSS}</style>`,
);

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

// Same as buildDom, but with the real stylesheet inlined and layout enabled
// so getComputedStyle reflects the actual cascade - buildDom's <link> is
// never fetched by jsdom, so it can't catch CSS bugs (see the "really
// display:none" tests below).
function buildVisualDom({ fetchImpl, clipboardImpl } = {}) {
  const dom = new JSDOM(HTML_WITH_INLINE_CSS, {
    runScripts: "outside-only",
    url: "http://localhost/",
    pretendToBeVisual: true,
  });
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
    luckyBtn: document.getElementById("lucky-btn"),
    input: document.getElementById("url-input"),
    submitBtn: document.getElementById("submit-btn"),
    formError: document.getElementById("form-error"),
    result: document.getElementById("result"),
    resultLink: document.getElementById("result-link"),
    copyBtn: document.getElementById("copy-btn"),
    copyStatus: document.getElementById("copy-status"),
    qrBtn: document.getElementById("qr-btn"),
    qrStatus: document.getElementById("qr-status"),
    qrPanel: document.getElementById("qr-panel"),
    qrImage: document.getElementById("qr-image"),
    qrDownload: document.getElementById("qr-download"),
  };
}

// jsdom never actually loads <img> resources, so a "click the QR button, then
// resolve/fail the load" test drives it by hand: wait for the src to be set
// (the real signal that a request was made), then dispatch the load/error
// event the browser would have fired itself.
async function waitForQrSrc(dom) {
  const { qrImage } = elements(dom);
  for (let i = 0; i < 50; i++) {
    if (qrImage.getAttribute("src")) {
      return;
    }
    await new Promise((resolve) => setTimeout(resolve, 0));
  }
  throw new Error("timed out waiting for #qr-image to get a src");
}

function fireQrLoad(dom) {
  const { qrImage } = elements(dom);
  qrImage.dispatchEvent(new dom.window.Event("load"));
}

function fireQrError(dom) {
  const { qrImage } = elements(dom);
  qrImage.dispatchEvent(new dom.window.Event("error"));
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

test("the footer tells the user links expire in 30 days", () => {
  const dom = buildDom();
  const { document } = elements(dom);

  const footer = document.querySelector("footer");
  assert.ok(footer, "expected a <footer> element");
  assert.match(footer.textContent, /expire in 30 days/i);
});

test("the footer invites users to leave a star on GitHub", () => {
  const dom = buildDom();
  const { document } = elements(dom);

  const sourceLink = document.querySelector(".source-link");
  assert.ok(sourceLink, "expected a source code link in the footer");
  assert.match(sourceLink.textContent, /leave a star on github/i);
  assert.equal(sourceLink.getAttribute("href"), "https://github.com/matteodelseppia/fold.link");
  assert.equal(sourceLink.getAttribute("target"), "_blank");
  assert.equal(sourceLink.getAttribute("rel"), "noopener noreferrer");
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

test("the submit button is disabled while pending and stays disabled for the same URL after success", async () => {
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
  assert.equal(submitBtn.disabled, true);
  assert.equal(submitBtn.querySelector(".btn-label").textContent, "Shortened");

  const { input } = elements(dom);
  input.value = "https://example.com/changed";
  input.dispatchEvent(new dom.window.Event("input", { bubbles: true }));
  assert.equal(submitBtn.disabled, false);
  assert.equal(submitBtn.querySelector(".btn-label").textContent, "Shorten");
});

test("a repeated submission for the same successful URL does not send another request", async () => {
  let calls = 0;
  const dom = buildDom({
    fetchImpl: async () => {
      calls += 1;
      return jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      });
    },
  });

  submitForm(dom, "https://example.com");
  await flush();
  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(calls, 1);
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

test("tab order follows the form controls, then the footer source link", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { document, luckyBtn, input, submitBtn, resultLink, copyBtn, qrBtn } = elements(dom);
  const sourceLink = document.querySelector(".source-link");

  submitForm(dom, "https://example.com");
  await flush();

  const focusable = Array.from(
    document.querySelectorAll("input, button, a[href]"),
  ).filter((el) => !el.hasAttribute("disabled") && !el.closest("[hidden]"));

  assert.deepEqual(focusable, [luckyBtn, input, resultLink, copyBtn, qrBtn, sourceLink]);
});

// --- QR code (optional, generated on demand) -------------------------------

test("the QR button is disabled until a short URL exists", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn } = elements(dom);

  assert.equal(qrBtn.disabled, true);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(qrBtn.disabled, false);
});

test("the QR panel and download link are hidden until requested", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrPanel, qrDownload } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(qrPanel.hidden, true);
  assert.equal(qrDownload.hasAttribute("href"), false);
});

test("clicking the QR button requests the alias's own QR endpoint, not before", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn, qrImage } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  assert.equal(qrImage.hasAttribute("src"), false);

  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);

  assert.equal(qrImage.getAttribute("src"), "/api/v1/links/abc12345/qr");
  assert.equal(qrBtn.disabled, true);
  assert.equal(qrBtn.classList.contains("is-loading"), true);
});

test("a successful QR load reveals the panel and enables the download link", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn, qrPanel, qrDownload } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  fireQrLoad(dom);

  assert.equal(qrPanel.hidden, false);
  assert.equal(qrBtn.disabled, false);
  assert.equal(qrBtn.classList.contains("is-loading"), false);
  assert.equal(qrBtn.getAttribute("aria-pressed"), "true");
  assert.equal(qrDownload.getAttribute("download"), "foldl-ink-abc12345.png");
  assert.match(qrDownload.getAttribute("href"), /\/api\/v1\/links\/abc12345\/qr$/);
});

test("clicking the QR button again toggles visibility without a second request", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn, qrPanel, qrImage } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  fireQrLoad(dom);
  const srcAfterFirstLoad = qrImage.getAttribute("src");

  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  assert.equal(qrPanel.hidden, true);
  assert.equal(qrBtn.getAttribute("aria-pressed"), "false");
  assert.equal(qrBtn.classList.contains("is-loading"), false);

  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  assert.equal(qrPanel.hidden, false);
  assert.equal(qrBtn.getAttribute("aria-pressed"), "true");
  assert.equal(qrImage.getAttribute("src"), srcAfterFirstLoad);
});

test("a failed QR load shows a retry-friendly message and re-enables the button", async () => {
  const dom = buildDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn, qrPanel, qrStatus } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  fireQrError(dom);

  assert.match(qrStatus.textContent, /couldn't generate/i);
  assert.equal(qrBtn.disabled, false);
  assert.equal(qrBtn.classList.contains("is-loading"), false);
  assert.equal(qrPanel.hidden, true);

  // The failure must be retryable, not a dead end.
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  assert.equal(qrBtn.classList.contains("is-loading"), true);
});

test("a new successful submission resets any previous QR state", async () => {
  let response = jsonResponse(201, {
    alias: "first111",
    shortUrl: "http://localhost/first111",
    destination: "https://example.com/one",
  });
  const dom = buildDom({ fetchImpl: async () => response });
  const { qrBtn, qrPanel, qrImage, qrStatus } = elements(dom);

  submitForm(dom, "https://example.com/one");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  fireQrLoad(dom);
  assert.equal(qrPanel.hidden, false);

  response = jsonResponse(201, {
    alias: "second22",
    shortUrl: "http://localhost/second22",
    destination: "https://example.com/two",
  });
  submitForm(dom, "https://example.com/two");
  await flush();

  assert.equal(qrPanel.hidden, true);
  assert.equal(qrImage.hasAttribute("src"), false);
  assert.equal(qrStatus.textContent, "");
  assert.equal(qrBtn.getAttribute("aria-pressed"), "false");
});

test("a stale QR load firing after a new submission is ignored", async () => {
  let response = jsonResponse(201, {
    alias: "first111",
    shortUrl: "http://localhost/first111",
    destination: "https://example.com/one",
  });
  const dom = buildDom({ fetchImpl: async () => response });
  const { qrBtn, qrPanel, qrImage, qrStatus } = elements(dom);

  submitForm(dom, "https://example.com/one");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);

  // A second shorten arrives while the first QR request is still in flight;
  // clearing #qr-image's src for the new result fires a stale "error" for
  // the *first* request, which must not clobber the fresh result's state.
  response = jsonResponse(201, {
    alias: "second22",
    shortUrl: "http://localhost/second22",
    destination: "https://example.com/two",
  });
  submitForm(dom, "https://example.com/two");
  await flush();
  fireQrError(dom);

  assert.equal(qrStatus.textContent, "");
  assert.equal(qrPanel.hidden, true);
  assert.equal(qrBtn.disabled, false);
});

// --- CSS actually renders the hidden state, not just the JS flag ----------
//
// Regression coverage for a real bug: `.qr-panel { display: flex; ... }`
// was an author-stylesheet rule for `display`, and an author rule for a
// property always wins over the browser's default `[hidden] { display:
// none }` - regardless of selector specificity, because that default lives
// in the lower-priority user-agent origin. `qrPanel.hidden` stayed `true`
// throughout (the JS state was never wrong), but the panel - with no QR
// image loaded yet - rendered visible immediately after shortening a link,
// before the QR button was ever clicked. Every other test above asserts
// only on `.hidden`/`.getAttribute("hidden")`, which is exactly why it
// didn't catch this: it needs the real stylesheet and a real computed
// style to fail the way the bug actually manifested.

test("the QR panel is really display:none while hidden, not just flagged hidden", async () => {
  const dom = buildVisualDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrPanel } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();

  assert.equal(qrPanel.hidden, true);
  assert.equal(dom.window.getComputedStyle(qrPanel).display, "none");
});

test("the QR panel actually renders once a QR code has loaded", async () => {
  const dom = buildVisualDom({
    fetchImpl: async () =>
      jsonResponse(201, {
        alias: "abc12345",
        shortUrl: "http://localhost/abc12345",
        destination: "https://example.com",
      }),
  });
  const { qrBtn, qrPanel } = elements(dom);

  submitForm(dom, "https://example.com");
  await flush();
  qrBtn.dispatchEvent(new dom.window.Event("click", { bubbles: true }));
  await waitForQrSrc(dom);
  fireQrLoad(dom);

  assert.equal(qrPanel.hidden, false);
  assert.notEqual(dom.window.getComputedStyle(qrPanel).display, "none");
});

test("every element the app toggles via the hidden property is really display:none while hidden", () => {
  const dom = buildVisualDom();
  const { document } = elements(dom);

  for (const id of ["result", "form-error", "qr-panel"]) {
    const el = document.getElementById(id);
    assert.equal(el.hidden, true, `expected #${id} to start hidden`);
    assert.equal(
      dom.window.getComputedStyle(el).display,
      "none",
      `expected #${id} to be display:none while hidden`,
    );
  }
});
