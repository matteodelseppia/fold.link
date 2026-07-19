# MVP-066: Implement the Redis key codec

## Description

Implement the versioned namespace used to convert aliases to Redis keys and prevent collisions with unrelated data.

## Acceptance Criteria

- The format includes the configured application prefix and schema version.
- Only contract-valid aliases can produce keys.
- Parsing or diagnostic formatting never reveals credentials.
- Testing: unit tests assert exact keys and reject invalid aliases and accidental double prefixes.
