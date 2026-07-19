# MVP-079: Add redirect storage-failure handling

## Description

Return a service-unavailable response rather than a false 404 when Redis lookup fails.

## Acceptance Criteria

- Redis failures return HTTP 503.
- The response is safe for a browser and contains no internal details.
- The failure is distinguishable from a missing alias in structured logs.
- Testing: MVC tests assert 503 on repository failure and 404 only on a true miss.
