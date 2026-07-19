# MVP-122: Rehearse staging secret failure recovery

## Description

Temporarily exercise the documented staging response to an invalid/expired Infisical bootstrap credential and restore service safely.

## Acceptance Criteria

- The candidate fails closed without printing secrets.
- Railway retains or restores the last healthy deployment according to policy.
- Credential correction restores readiness and generates expected operational evidence.
- Testing: verify alerting, redacted logs, failed readiness, and successful recovery; do not perform this in production.
