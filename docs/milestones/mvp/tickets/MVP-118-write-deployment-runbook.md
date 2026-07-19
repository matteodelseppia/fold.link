# MVP-118: Write the deployment runbook

## Description

Document normal pipeline release, approval, environment verification, failure diagnosis, and immutable-digest rollback procedures.

## Acceptance Criteria

- Commands and console paths are exact but contain no credentials.
- The runbook identifies stop conditions and responsible roles.
- Staging and production steps cannot be confused.
- Testing: a second operator follows the staging release and rollback sections without undocumented knowledge.
