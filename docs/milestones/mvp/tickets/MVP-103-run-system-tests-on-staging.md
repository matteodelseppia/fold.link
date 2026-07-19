# MVP-103: Run system tests on staging

## Description
Run the remote-safe system-test subset against the newly deployed staging digest before promotion.

## Acceptance Criteria
- Tests target the configured staging URL and verify environment identity.
- Test data uses unique aliases and harmless destinations.
- Failures block production promotion and retain redacted HTTP evidence.
- Testing: execute the job successfully, then prove a fixture contract mismatch blocks promotion.
