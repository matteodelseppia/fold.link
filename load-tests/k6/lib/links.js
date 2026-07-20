// Thin helpers over the link-creation/redirect API, shared by the k6
// scenarios. Mirrors the contract exercised in test/system/links.test.mjs.
import http from "k6/http";
import { BASE_URL, ALIAS_PATTERN, randomDestination } from "./config.js";

export function createLink(destination) {
  return http.post(`${BASE_URL}/api/v1/links`, JSON.stringify({ url: destination }), {
    headers: { "Content-Type": "application/json" },
    tags: { name: "create_link" },
  });
}

export function getAlias(alias) {
  return http.get(`${BASE_URL}/${alias}`, {
    redirects: 0,
    tags: { name: "redirect" },
  });
}

export function isValidAlias(alias) {
  return typeof alias === "string" && ALIAS_PATTERN.test(alias);
}

// Pre-creates a pool of mappings for scenarios that only need to *read*
// aliases (redirect.js, mixed.js). This setup traffic is intentionally
// not tagged/recorded into the scenarios' own redirect_duration /
// redirect_errors metrics, so it never pollutes redirect measurements.
export function createAliasPool(poolSize) {
  const pool = [];
  for (let i = 0; i < poolSize; i++) {
    const destination = randomDestination("pool");
    const response = createLink(destination);
    if (response.status !== 201) {
      throw new Error(`[k6 setup] failed to pre-create alias pool entry ${i}: HTTP ${response.status} ${response.body}`);
    }
    const body = response.json();
    if (!isValidAlias(body.alias)) {
      throw new Error(`[k6 setup] pre-created alias "${body.alias}" does not match the expected alias shape`);
    }
    pool.push({ alias: body.alias, destination });
  }
  return pool;
}
