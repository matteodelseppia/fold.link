# MVP-047: Configure public service URLs

## Description
Allocate Railway staging and production service URLs and set the matching public-base-url secrets in Infisical.

## Acceptance Criteria
- URLs use HTTPS and differ by environment.
- Generated short URLs can be formed without request-header trust.
- Staging configuration never emits the production hostname and vice versa.
- Testing: start each profile with its injected value and verify a fixture short URL uses the expected origin.
