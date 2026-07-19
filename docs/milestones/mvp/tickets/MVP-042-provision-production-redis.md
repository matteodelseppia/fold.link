# MVP-042: Provision production Redis

## Description

Provision a dedicated production Railway Redis service and store its connection values in Infisical production.

## Acceptance Criteria

- Production Redis is not shared with staging or CI.
- Authentication and supported transport security are enabled.
- Only the production runtime identity can retrieve its credentials.
- Testing: production-scoped connectivity succeeds; staging and CI identities are denied.
