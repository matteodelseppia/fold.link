# MVP-121: Rehearse staging application rollback

## Description

Practice rolling staging from a candidate digest to the prior healthy digest and forward again without changing Redis data.

## Acceptance Criteria

- Both digests are recorded and verified after deployment.
- A pre-existing alias works before rollback, after rollback, and after roll-forward.
- Timings and unexpected manual actions update the runbook.
- Testing: system smoke and persistence checks pass at all three checkpoints.
