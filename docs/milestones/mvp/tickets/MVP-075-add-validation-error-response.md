# MVP-075: Add the validation error response

## Description
Translate malformed JSON, missing fields, and invalid destinations into one stable, user-safe error contract.

## Acceptance Criteria
- Invalid requests return HTTP 400 with a code and clear field message.
- Parser or framework stack details are not exposed.
- All validation paths use the same JSON structure.
- Testing: MVC tests cover empty body, malformed JSON, missing URL, blank URL, unsupported scheme, and invalid host.
