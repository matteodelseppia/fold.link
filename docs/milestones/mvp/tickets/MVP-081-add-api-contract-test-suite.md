# MVP-081: Add the API contract test suite

## Description

Create black-box HTTP contract tests for link creation, redirect, validation, not-found, and unavailable-storage behavior.

## Acceptance Criteria

- Tests use only public HTTP behavior and exact documented schemas.
- F01, F02, F03, and F05 are each identified by test name.
- The suite can target a random local application port.
- Testing: run against the packaged application with disposable Redis and demonstrate one intentional contract change is caught.
