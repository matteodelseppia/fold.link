# MVP-132: Configure registry retention policy

## Description

Configure GitLab container registry cleanup so rollback-worthy immutable releases are retained while unneeded intermediate artifacts are bounded.

## Acceptance Criteria

- Deployed production and a documented number/age of prior digests are protected.
- Mutable convenience tags cannot be the only retention anchor.
- Cleanup schedule and rollback implications are documented.
- Testing: use policy preview to verify current staging, production, and prior rollback digests would be retained.
