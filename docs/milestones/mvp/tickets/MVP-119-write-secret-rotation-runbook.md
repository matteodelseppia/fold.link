# MVP-119: Write the secret-rotation runbook

## Description

Document zero- or low-downtime rotation for Redis, Infisical runtime identities, CI identity, Railway registry credentials, and deployment tokens.

## Acceptance Criteria

- Each credential has owner, order of operations, validation, revocation, and rollback.
- The bootstrap-secret exception is handled explicitly.
- No current secret value appears in the document.
- Testing: rehearse one non-production runtime identity rotation and verify old access is revoked.
