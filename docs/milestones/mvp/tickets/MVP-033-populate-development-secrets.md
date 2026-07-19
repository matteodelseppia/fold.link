# MVP-033: Populate Infisical development secrets

## Description

Add the local Redis and public-base-url values to the Infisical development environment.

## Acceptance Criteria

- All required development keys from the schema exist.
- Values target only local services and contain no staging/production credentials.
- Access is limited to developer identities.
- Testing: `infisical run` starts the local application without a checked-in `.env` containing real values.
