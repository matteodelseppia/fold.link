# MVP-062: Implement the secure alias generator

## Description

Generate eight-character aliases from a URL-safe alphabet using a cryptographically secure random source.

## Acceptance Criteria

- Output length and alphabet come from validated configuration.
- Generation is unbiased or documents negligible bias from the chosen primitive.
- No timestamp, counter, destination hash, or predictable PRNG state is exposed.
- Testing: unit tests inject a deterministic random source to verify length, alphabet, and byte-to-character behavior.
