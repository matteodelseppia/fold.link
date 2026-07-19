# MVP-045: Configure Railway health and restart policy

## Description

Configure both application services to use readiness health checks, bounded rollout timeouts, and an intentional restart policy.

## Acceptance Criteria

- Health-check path matches the actuator readiness endpoint.
- An unhealthy candidate cannot replace the last healthy deployment.
- Restart behavior distinguishes process failure from operator stop where Railway permits.
- Testing: deploy a deliberately unhealthy staging image and verify rollout failure without reporting it healthy.
