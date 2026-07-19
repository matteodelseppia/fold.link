# MVP-064: Define the URL-mapping repository port

## Description

Define a domain-facing interface for atomic create-if-absent and alias lookup operations.

## Acceptance Criteria

- Create distinguishes stored, collision, and storage-failure outcomes.
- Lookup distinguishes not found from storage failure.
- No Redis-specific types leak into service code.
- Testing: compile-time fake repository tests demonstrate every outcome can be represented.
