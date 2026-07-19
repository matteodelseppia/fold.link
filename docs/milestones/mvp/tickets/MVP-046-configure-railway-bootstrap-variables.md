# MVP-046: Configure Railway bootstrap variables

## Description

Add only the approved Infisical authentication bootstrap variables to each Railway application service.

## Acceptance Criteria

- Staging and production use different machine identities.
- Variables are marked secret and scoped to the correct environment.
- No Redis password, URL-mapping data, or GitLab token is stored directly in Railway.
- Testing: each service can fetch its own Infisical path, and a cross-environment fetch is denied.
