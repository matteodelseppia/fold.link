# MVP-131: Configure GitLab deployment credentials

## Description

Store the minimum Railway and Infisical authentication bootstrap values required by protected deployment jobs in masked GitLab variables.

## Acceptance Criteria

- Staging and production scope/protection follow the bootstrap-boundary document.
- Merge-request jobs cannot read protected production variables.
- Tokens have minimum permissions, owners, and expiry/rotation records.
- Testing: a protected dry-run authenticates and an unprotected branch job proves the values are unavailable.
