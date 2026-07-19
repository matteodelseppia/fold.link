# MVP-098: Add the unknown-alias system test

## Description

Verify a contract-valid but absent alias returns the required not-found behavior through the deployed HTTP stack.

## Acceptance Criteria

- The chosen alias is confirmed absent in the isolated test namespace.
- The response is 404 and does not redirect.
- The body and headers match the public error contract.
- Testing: run locally and against staging; the test is explicitly traced to F03.
