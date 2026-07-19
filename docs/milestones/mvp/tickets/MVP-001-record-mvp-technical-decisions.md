# MVP-001: Record MVP technical decisions

## Description
Create a short architecture decision record that fixes the MVP API paths, response shapes, redirect status, alias format, supported URL schemes, Redis key format, and Java build tool so later tickets do not make incompatible assumptions.

## Acceptance Criteria
- The record specifies `POST /api/links`, `GET /{alias}`, `302 Found`, HTTP/HTTPS-only destinations, an eight-character URL-safe random alias, and a versioned Redis key prefix.
- The record selects Gradle and states that Spring Boot serves the static frontend.
- Each choice links to the applicable requirement or design section.
- Testing: a reviewer can derive every public request, response, and storage contract from the record without an unresolved placeholder.
