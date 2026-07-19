# MVP-058: Implement URL scheme validation

## Description

Implement destination parsing that accepts absolute HTTP and HTTPS URLs and rejects missing, relative, malformed, or unsupported schemes.

## Acceptance Criteria

- Scheme comparison is case-insensitive and normalized.
- Credentials-in-URL and dangerous non-web schemes follow the recorded policy.
- Validation returns a domain error rather than a low-level parser exception.
- Testing: parameterized tests cover valid HTTP/HTTPS and representative malformed, relative, `javascript:`, `file:`, and blank inputs.
