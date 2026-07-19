# MVP-010: Add documentation linting

## Description

Configure a lightweight Markdown linter for repository and milestone documentation.

## Acceptance Criteria

- A pinned linter configuration and ignore file are committed.
- Mermaid fences and long table rows are handled intentionally.
- One documented command checks all Markdown files.
- Testing: the command passes current documents and fails on a temporary malformed heading sequence.
