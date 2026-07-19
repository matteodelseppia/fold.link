# MVP-030: Run container smoke testing in CI

## Description

Add a post-build GitLab job that executes the container smoke-test script before any registry publication.

## Acceptance Criteria

- The job consumes the exact JAR or image candidate from the build pipeline.
- Failure blocks image publication.
- Diagnostic container logs are retained on failure.
- Testing: a valid candidate passes and a branch with a broken health path cannot reach the publish stage.
