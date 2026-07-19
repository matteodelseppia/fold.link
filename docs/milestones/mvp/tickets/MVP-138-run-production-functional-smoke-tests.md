# MVP-138: Run production functional smoke tests

## Description
Run a low-impact production-safe check of page load, valid link creation, redirect, invalid URL rejection, and unknown alias handling.

## Acceptance Criteria
- Test destinations are controlled and contain no sensitive information.
- F01–F05 each receive a passing smoke result.
- Created test aliases are recorded as harmless persistent test data.
- Testing: execute the production-safe suite against the canonical HTTPS domain and retain redacted results tied to the digest.
