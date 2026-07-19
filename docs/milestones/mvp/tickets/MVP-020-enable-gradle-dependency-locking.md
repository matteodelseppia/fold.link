# MVP-020: Enable dependency locking

## Description

Enable and commit Gradle dependency locks so local, CI, and container builds resolve identical transitive versions.

## Acceptance Criteria

- All resolvable configurations used by the application and tests are locked.
- The lock refresh command is documented.
- Dynamic and snapshot dependency versions are rejected.
- Testing: a build succeeds with locks enabled and fails when a declared dependency conflicts with the lock state.
