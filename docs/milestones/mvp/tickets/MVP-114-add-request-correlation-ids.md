# MVP-114: Add request correlation identifiers

## Description

Accept or generate bounded safe request IDs, include them in responses and structured logs, and clear logging context after each request.

## Acceptance Criteria

- Missing/invalid IDs produce a generated value.
- Valid IDs round-trip in the documented header.
- Concurrent requests do not leak IDs across threads.
- Testing: MVC and concurrency tests verify generation, validation, response header, log inclusion, and context cleanup.
