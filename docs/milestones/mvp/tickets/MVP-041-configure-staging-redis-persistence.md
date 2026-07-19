# MVP-041: Configure staging Redis persistence

## Description
Enable and document Railway staging Redis persistence in accordance with NF01.

## Acceptance Criteria
- AOF or the supported durable persistence mode is enabled.
- Volume attachment, retention behavior, and maintenance implications are recorded.
- Restarting the Redis service does not intentionally discard its data volume.
- Testing: write a marker, restart/redeploy Redis, and verify the marker remains.
