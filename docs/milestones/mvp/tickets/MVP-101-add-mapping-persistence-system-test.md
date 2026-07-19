# MVP-101: Add the mapping-persistence system test

## Description

Prove a created alias remains usable after application and Redis restart/redeployment without deleting the data volume.

## Acceptance Criteria

- The test creates a unique mapping and records its expected destination.
- Application-only restart and persistence-enabled Redis restart are separate checks.
- The same alias redirects correctly after both events.
- Testing: run locally with Compose and in staging during the persistence rehearsal; trace evidence to NF01.
