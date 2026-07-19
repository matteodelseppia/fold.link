# MVP-144: Create MVP release notes and immutable tag

## Description
Write concise release notes for scope, architecture, known limitations, operations, and verification, then create the repository's MVP release tag at the deployed commit.

## Acceptance Criteria
- The tag commit matches the production image commit label and digest record.
- Notes list F01–F05, operational dependencies, and non-goals without overstating guarantees.
- Links to pipeline, SBOM, runbooks, and traceability evidence resolve.
- Testing: independently compare Git tag, GitLab pipeline SHA, image label, Railway digest, and release notes; all identify one release.
