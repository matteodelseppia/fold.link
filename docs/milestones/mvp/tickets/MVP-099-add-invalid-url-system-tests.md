# MVP-099: Add invalid-URL system tests

## Description

Verify representative malformed, relative, and dangerous URL inputs are rejected through the real API.

## Acceptance Criteria

- Blank, malformed, relative, unsupported-scheme, hostless, and oversized inputs are covered.
- Each returns the documented 4xx response and creates no retrievable mapping.
- The test is traced to F05.
- Testing: run locally and staging and confirm Redis key count does not increase for the isolated test prefix.
