(() => {
  "use strict";

  const form = document.getElementById("shorten-form");
  const input = document.getElementById("url-input");
  const submitBtn = document.getElementById("submit-btn");
  const btnLabel = submitBtn.querySelector(".btn-label");
  const formError = document.getElementById("form-error");
  const result = document.getElementById("result");
  const resultLink = document.getElementById("result-link");
  const copyBtn = document.getElementById("copy-btn");
  const copyStatus = document.getElementById("copy-status");
  const copyIcon = copyBtn.querySelector(".icon-copy");
  const checkIcon = copyBtn.querySelector(".icon-check");
  const qrBtn = document.getElementById("qr-btn");
  const qrStatus = document.getElementById("qr-status");
  const qrPanel = document.getElementById("qr-panel");
  const qrImage = document.getElementById("qr-image");
  const qrDownload = document.getElementById("qr-download");

  const SCHEME_PATTERN = /^https?:\/\//i;

  // Messages for API error codes (ADR-001) that aren't already carried by the
  // server's own VALIDATION_ERROR message. Deliberately generic - never echo
  // raw server/network details back to the user.
  const API_ERROR_MESSAGES = {
    STORAGE_ERROR: "The service is temporarily unavailable. Please try again in a moment.",
  };
  const FALLBACK_MESSAGE = "Something went wrong. Please try again.";

  let pending = false;
  let currentAlias = null;
  let qrRequested = false;
  let qrRequestGeneration = 0;

  function validate(value) {
    const trimmed = (value || "").trim();
    if (!trimmed) {
      return "Enter a URL to shorten.";
    }
    if (!SCHEME_PATTERN.test(trimmed)) {
      return "URL must start with http:// or https://";
    }
    return null;
  }

  function messageForApiError(status, body) {
    if (body && typeof body.error === "string") {
      if (body.error === "VALIDATION_ERROR" && typeof body.message === "string" && body.message) {
        return body.message;
      }
      if (API_ERROR_MESSAGES[body.error]) {
        return API_ERROR_MESSAGES[body.error];
      }
    }
    if (status === 503 || status >= 500) {
      return API_ERROR_MESSAGES.STORAGE_ERROR;
    }
    return FALLBACK_MESSAGE;
  }

  function showError(message) {
    formError.textContent = message;
    formError.hidden = false;
    input.setAttribute("aria-invalid", "true");
  }

  function clearError() {
    formError.hidden = true;
    formError.textContent = "";
    input.setAttribute("aria-invalid", "false");
  }

  function resetQr() {
    currentAlias = null;
    qrRequested = false;
    // Invalidates any in-flight request's load/error handlers below - clearing
    // qrImage's src while a load is pending fires a stale "error" event that
    // must not be allowed to touch a since-replaced result.
    qrRequestGeneration += 1;
    qrPanel.hidden = true;
    qrImage.removeAttribute("src");
    qrDownload.removeAttribute("href");
    qrStatus.textContent = "";
    qrBtn.classList.remove("is-loading");
    qrBtn.setAttribute("aria-pressed", "false");
    qrBtn.setAttribute("aria-label", "Show QR code");
  }

  function hideResult() {
    result.hidden = true;
    resultLink.textContent = "";
    resultLink.removeAttribute("href");
    copyBtn.disabled = true;
    copyStatus.textContent = "";
    resetQr();
    qrBtn.disabled = true;
  }

  function showResult(data) {
    resultLink.textContent = data.shortUrl;
    resultLink.setAttribute("href", data.shortUrl);
    result.hidden = false;
    copyBtn.disabled = false;
    copyStatus.textContent = "";
    resetQr();
    currentAlias = data.alias;
    qrBtn.disabled = false;
  }

  function setPending(isPending) {
    pending = isPending;
    submitBtn.disabled = isPending;
    submitBtn.classList.toggle("is-loading", isPending);
    btnLabel.textContent = isPending ? "Shortening…" : "Shorten";
  }

  async function submit(url) {
    setPending(true);
    hideResult();
    try {
      const response = await fetch("/api/v1/links", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url }),
      });

      let body = null;
      try {
        body = await response.json();
      } catch {
        body = null;
      }

      if (response.ok) {
        clearError();
        showResult(body);
      } else {
        showError(messageForApiError(response.status, body));
        input.focus();
      }
    } catch {
      showError("Network error — check your connection and try again.");
      input.focus();
    } finally {
      setPending(false);
    }
  }

  form.addEventListener("submit", (event) => {
    event.preventDefault();
    if (pending) {
      return;
    }

    const validationMessage = validate(input.value);
    if (validationMessage) {
      hideResult();
      showError(validationMessage);
      input.focus();
      return;
    }

    clearError();
    submit(input.value.trim());
  });

  let copyResetTimer = null;

  function fallbackCopy(text) {
    const textarea = document.createElement("textarea");
    textarea.value = text;
    textarea.setAttribute("readonly", "");
    textarea.style.position = "fixed";
    textarea.style.top = "-1000px";
    textarea.style.opacity = "0";
    document.body.appendChild(textarea);
    textarea.select();
    textarea.setSelectionRange(0, text.length);
    let ok = false;
    try {
      ok = document.execCommand("copy");
    } catch {
      ok = false;
    }
    document.body.removeChild(textarea);
    if (!ok) {
      throw new Error("execCommand copy failed");
    }
  }

  async function copyToClipboard(text) {
    if (navigator.clipboard && typeof navigator.clipboard.writeText === "function") {
      try {
        await navigator.clipboard.writeText(text);
        return;
      } catch {
        // Clipboard API present but blocked (e.g. insecure context) — fall through.
      }
    }
    fallbackCopy(text);
  }

  copyBtn.addEventListener("click", async () => {
    const url = resultLink.getAttribute("href");
    try {
      await copyToClipboard(url);
      copyStatus.textContent = "Copied!";
      copyIcon.hidden = true;
      checkIcon.hidden = false;
      clearTimeout(copyResetTimer);
      copyResetTimer = setTimeout(() => {
        copyIcon.hidden = false;
        checkIcon.hidden = true;
      }, 2000);
    } catch {
      copyStatus.textContent = "Copy failed — select the link text above and copy manually.";
    }
  });

  function requestQr() {
    const alias = currentAlias;
    const generation = qrRequestGeneration;
    qrRequested = true;
    qrBtn.disabled = true;
    qrBtn.classList.add("is-loading");
    qrStatus.textContent = "";

    function cleanup() {
      qrImage.removeEventListener("load", onLoad);
      qrImage.removeEventListener("error", onError);
    }

    function onLoad() {
      cleanup();
      if (generation !== qrRequestGeneration) {
        return;
      }
      qrBtn.disabled = false;
      qrBtn.classList.remove("is-loading");
      qrPanel.hidden = false;
      qrBtn.setAttribute("aria-pressed", "true");
      qrBtn.setAttribute("aria-label", "Hide QR code");
      qrDownload.setAttribute("href", qrImage.src);
      qrDownload.setAttribute("download", `foldl-ink-${alias}.png`);
    }

    function onError() {
      cleanup();
      if (generation !== qrRequestGeneration) {
        return;
      }
      qrRequested = false;
      qrBtn.disabled = false;
      qrBtn.classList.remove("is-loading");
      qrStatus.textContent = "Couldn't generate a QR code — try again.";
    }

    qrImage.addEventListener("load", onLoad);
    qrImage.addEventListener("error", onError);
    qrImage.src = `/api/v1/links/${alias}/qr`;
  }

  qrBtn.addEventListener("click", () => {
    if (!currentAlias) {
      return;
    }
    if (!qrRequested) {
      requestQr();
      return;
    }

    const showing = qrPanel.hidden;
    qrPanel.hidden = !showing;
    qrBtn.setAttribute("aria-pressed", String(showing));
    qrBtn.setAttribute("aria-label", showing ? "Hide QR code" : "Show QR code");
  });

  // Exposed only so the Node/jsdom test suite can exercise the pure logic
  // directly; harmless in production, never read by the app itself.
  window.__foldLinkApp = { validate, messageForApiError };
})();
