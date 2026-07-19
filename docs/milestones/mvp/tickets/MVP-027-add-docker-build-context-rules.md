# MVP-027: Add Docker build-context rules

## Description
Add `.dockerignore` entries that keep VCS data, secrets, local builds, test reports, IDE files, and documentation-only artifacts out of the build context.

## Acceptance Criteria
- `.git`, `.env*`, `build`, `.gradle`, and local test outputs are excluded.
- Wrapper and application sources remain available to the build stage.
- No secret-like fixture enters the sent build context.
- Testing: inspect a plain-progress build log or context archive and verify included/excluded representative files.
