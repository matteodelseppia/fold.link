# MVP-031: Create the Infisical project and environments

## Description
Create an Infisical project with isolated `development`, `staging`, and `production` environments and document non-secret identifiers.

## Acceptance Criteria
- Each environment exists with least-privilege access boundaries.
- The project identifier and environment slugs are recorded outside secret storage.
- Production access is restricted to the production runtime and authorized operators.
- Testing: list secrets as each environment role and verify it cannot read a different environment.
