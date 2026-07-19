# MVP-068: Implement Redis alias lookup

## Description

Implement read-only lookup of a destination by alias using the versioned key codec.

## Acceptance Criteria

- Existing values return the domain mapping.
- Missing keys return not found without an exception.
- Malformed stored data becomes a controlled storage failure.
- Testing: integration tests cover hit, miss, Unicode/encoded URL round trip, malformed value, and unavailable Redis.
