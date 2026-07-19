# MVP-095: Add HTTP cache-control policy

## Description

Define cache headers so redirects and API errors cannot become dangerously stale while static assets behave predictably.

## Acceptance Criteria

- Redirect, creation, validation, 404, and 503 responses use intentional cache directives.
- HTML supports rapid release updates.
- The policy is documented beside the HTTP contract.
- Testing: HTTP tests assert exact cache headers for each response class.
