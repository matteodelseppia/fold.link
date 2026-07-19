# MVP-070: Add repository contract tests

## Description
Create a reusable contract suite for URL-mapping repository implementations and run it against the Redis adapter.

## Acceptance Criteria
- The suite covers create, collision, hit, miss, persistence, and failure semantics.
- Test aliases and keys are isolated per run.
- The Redis adapter passes without behavior-specific exemptions.
- Testing: intentionally break collision handling and confirm the contract suite fails.
