# MVP-135: Run the full staging acceptance suite

## Description

Execute every functional, system, accessibility, security-header, persistence-safe, and bounded load check against the release-candidate digest in staging.

## Acceptance Criteria

- F01–F05 and NF01–NF04 each have passing linked evidence.
- No test is skipped without reopening its implementation or infrastructure ticket.
- The exact image digest and configuration version are recorded.
- Testing: all automated suites and the external configuration checklist pass in one release pipeline.
