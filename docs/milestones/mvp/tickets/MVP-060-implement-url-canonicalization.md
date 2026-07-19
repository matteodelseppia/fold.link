# MVP-060: Implement URL canonicalization

## Description

Canonicalize accepted destinations only enough for safe storage and redirection without changing resource semantics.

## Acceptance Criteria

- Scheme and host casing are normalized.
- Path, query ordering, percent encoding, and fragment semantics are preserved according to the decision record.
- The canonical value round-trips through the Redis codec.
- Testing: table-driven tests assert exact canonical outputs for edge-case URLs.
