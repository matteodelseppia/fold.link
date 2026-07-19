# MVP-084: Implement frontend link submission

## Description
Add dependency-free JavaScript that submits the form to `POST /api/links` and handles the success payload.

## Acceptance Criteria
- JSON and content headers match the API contract.
- Repeat submissions cannot double-send while one request is pending.
- The UI returns to an interactive state after success or failure.
- Testing: Node tests with mocked `fetch` verify request shape, pending-state behavior, and success rendering.
