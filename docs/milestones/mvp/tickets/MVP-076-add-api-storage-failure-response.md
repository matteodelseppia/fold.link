# MVP-076: Add the API storage-failure response

## Description
Translate Redis unavailability and exhausted alias retries into stable service-unavailable responses.

## Acceptance Criteria
- Failures return HTTP 503 with distinct safe error codes.
- Responses contain no Redis host, credential, key, or stack trace.
- Unexpected exceptions retain a generic 500 fallback.
- Testing: MVC tests exercise repository failure, collision exhaustion, and unexpected exception mappings.
