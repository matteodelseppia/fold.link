# MVP-025: Add the actuator health endpoint

## Description

Expose a minimal health endpoint for container and Railway probes without exposing sensitive actuator data.

## Acceptance Criteria

- Liveness and readiness groups are enabled at documented paths.
- Only health information is publicly exposed.
- Readiness includes Redis while liveness does not depend on Redis.
- Testing: integration tests verify healthy responses and a readiness failure when Redis is unavailable.
