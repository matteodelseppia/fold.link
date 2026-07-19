# MVP-077: Add the redirect controller

## Description
Expose `GET /{alias}` and redirect an existing alias to its stored destination.

## Acceptance Criteria
- A hit returns `302 Found` and one correct `Location` header.
- Query strings and fragments in the destination are preserved.
- The response body does not leak internal mapping data.
- Testing: MVC tests cover HTTP and HTTPS destinations with encoded paths, queries, and fragments.
