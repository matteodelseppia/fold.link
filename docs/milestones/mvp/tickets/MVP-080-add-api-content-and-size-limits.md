# MVP-080: Add API content and size limits

## Description

Constrain link-creation requests to JSON and a documented maximum destination/request size.

## Acceptance Criteria

- Unsupported content types return 415.
- Oversized requests or URLs return a controlled 4xx response.
- Limits accept the documented maximum valid URL.
- Testing: MVC tests cover exact-boundary, over-boundary, wrong media type, and valid JSON cases.
