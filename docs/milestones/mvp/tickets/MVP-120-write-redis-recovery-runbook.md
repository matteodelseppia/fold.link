# MVP-120: Write the Redis recovery runbook

## Description

Document persistence expectations, available backups/snapshots, restore procedure, data-loss boundaries, and validation for Railway Redis.

## Acceptance Criteria

- Provider limitations and recovery point expectations are explicit.
- Restore uses a new staging instance before any production action.
- Post-restore alias verification and secret update steps are included.
- Testing: perform a staging restore or provider-supported recovery rehearsal and verify a known mapping.
