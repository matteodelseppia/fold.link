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

  const SCHEME_PATTERN = /^https?:\/\//i;

  // Messages for API error codes (ADR-001) that aren't already carried by the
  // server's own VALIDATION_ERROR message. Deliberately generic - never echo
  // raw server/network details back to the user.
  const API_ERROR_MESSAGES = {
    STORAGE_ERROR: "The service is temporarily unavailable. Please try again in a moment.",
  };
  const FALLBACK_MESSAGE = "Something went wrong. Please try again.";

  let pending = false;

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

  function hideResult() {
    result.hidden = true;
    resultLink.textContent = "";
    resultLink.removeAttribute("href");
    copyBtn.disabled = true;
    copyStatus.textContent = "";
  }

  function showResult(data) {
    resultLink.textContent = data.shortUrl;
    resultLink.setAttribute("href", data.shortUrl);
    result.hidden = false;
    copyBtn.disabled = false;
    copyStatus.textContent = "";
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

  copyBtn.addEventListener("click", async () => {
    const url = resultLink.getAttribute("href");
    try {
      await navigator.clipboard.writeText(url);
      copyStatus.textContent = "Copied!";
    } catch {
      copyStatus.textContent = "Copy failed — select the link text above and copy manually.";
    }
  });

  // Exposed only so the Node/jsdom test suite can exercise the pure logic
  // directly; harmless in production, never read by the app itself.
  window.__foldLinkApp = { validate, messageForApiError };
})();
