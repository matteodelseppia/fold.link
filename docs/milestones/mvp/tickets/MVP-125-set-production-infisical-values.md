# MVP-125: Set final production Infisical values

## Description
Populate and review the production public base URL and Redis runtime values immediately before feature release.

## Acceptance Criteria
- Every required key from the secret schema exists exactly once.
- Values reference only production domain and production Redis resources.
- Access audit shows only approved identities.
- Testing: a redacted preflight validates names, formats, connectivity, and public-origin construction without printing values.
