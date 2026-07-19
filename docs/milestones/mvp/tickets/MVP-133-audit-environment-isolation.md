# MVP-133: Audit environment isolation

## Description
Perform a pre-release audit of local, CI, staging, and production URLs, Redis instances, Infisical identities, Railway services, and deployment credentials.

## Acceptance Criteria
- A matrix shows every runtime-to-resource connection and authorized identity.
- No staging/CI principal can access production Redis or secrets.
- No production service points to staging resources.
- Testing: execute the least-privilege negative checks in the matrix and retain redacted results.
