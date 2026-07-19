# MVP-067: Implement atomic Redis create-if-absent

## Description
Implement URL mapping creation with Redis `SET NX` semantics so concurrent requests cannot overwrite an alias.

## Acceptance Criteria
- Successful writes have no expiry.
- Existing keys return collision without changing their value.
- Redis failures map to the repository storage-failure outcome.
- Testing: integration tests verify first write, collision preservation, no TTL, and unavailable-Redis behavior.
