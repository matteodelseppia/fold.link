# MVP-134: Rehearse feature persistence in staging

## Description

Create a real staging short link, redeploy the application, restart staging Redis using the supported procedure, and verify the mapping remains.

## Acceptance Criteria

- The mapping works before and after both disruptions.
- The deployed image digest is unchanged across the Redis restart.
- Evidence is linked to NF01 and the recovery runbook.
- Testing: the remote persistence system test passes at each checkpoint.
