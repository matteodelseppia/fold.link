# MVP-085: Add frontend input validation

## Description
Provide immediate client-side feedback for blank, malformed, and unsupported-scheme URLs while retaining server authority.

## Acceptance Criteria
- HTTP and HTTPS inputs can be submitted.
- Invalid input focuses the field and shows a clear message.
- Client validation never replaces handling an API 400 response.
- Testing: Node DOM tests cover valid schemes, blank, malformed, and unsupported schemes and ensure invalid input does not call `fetch`.
