# MVP-072: Implement bounded collision retry

## Description

Extend link creation to generate a new alias after atomic-store collisions up to the configured retry limit.

## Acceptance Criteria

- Each collision uses a newly generated candidate.
- Retry exhaustion returns a controlled service-unavailable domain error.
- Storage failures are not retried as alias collisions.
- Testing: deterministic unit tests cover zero collisions, several collisions then success, exhaustion, and immediate storage failure.
