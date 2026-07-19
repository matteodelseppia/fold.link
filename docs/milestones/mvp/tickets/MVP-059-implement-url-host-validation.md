# MVP-059: Implement URL host validation

## Description

Extend destination validation to require a syntactically valid host and intentional port handling.

## Acceptance Criteria

- Hostless absolute-looking inputs are rejected.
- Internationalized names and IPv4/IPv6 behavior are explicitly supported or rejected consistently.
- URL fragments and paths do not bypass host validation.
- Testing: parameterized tests cover DNS names, ports, localhost policy, IPv4, bracketed IPv6, invalid labels, and missing hosts.
