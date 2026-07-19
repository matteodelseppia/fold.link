# MVP-043: Configure production Redis persistence

## Description
Enable durable persistence and volume protection for production Redis.

## Acceptance Criteria
- The supported AOF/durable mode and volume are enabled.
- Deletion protection or an equivalent operator control is configured where available.
- Backup/restore limitations are recorded for the release runbook.
- Testing: persist a non-sensitive marker across a controlled restart and retain evidence.
