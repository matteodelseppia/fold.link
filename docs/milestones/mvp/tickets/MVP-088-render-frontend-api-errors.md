# MVP-088: Render frontend API errors

## Description
Map API validation, not-found-independent creation failures, service unavailability, malformed responses, and network errors to clear user messages.

## Acceptance Criteria
- Known API messages are displayed safely and unknown details are not echoed.
- A failed request clears stale success content and permits retry.
- Network and 503 errors explain that the user may retry.
- Testing: Node tests cover 400, 503, 500, invalid JSON, rejected fetch, and recovery on the next success.
