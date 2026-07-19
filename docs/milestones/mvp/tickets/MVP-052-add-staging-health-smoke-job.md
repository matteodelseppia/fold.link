# MVP-052: Add the staging health smoke job

## Description
Add a post-deploy CI job that checks staging HTTPS reachability, readiness, and expected minimal response behavior.

## Acceptance Criteria
- Checks use bounded retries and fail on non-HTTPS redirects or wrong environment identity.
- The deployed commit identifier is verified where exposed safely.
- Failure prevents production promotion.
- Testing: the job passes against staging and fails against a fixture endpoint with the wrong health response.
