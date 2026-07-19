# MVP-017: Add a safe local environment example

## Description
Commit an example environment file listing every locally required non-secret variable and secret placeholder.

## Acceptance Criteria
- Names match the typed Spring configuration and Compose service.
- Placeholder values cannot access staging or production.
- The file explains that real values come from Infisical and must not be committed.
- Testing: copy the example to the ignored local filename, inject fixture values, and successfully start the application.
