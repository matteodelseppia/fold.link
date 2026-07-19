# MVP-009: Pin local tool versions

## Description

Add the repository's supported Java, Node.js, Gradle-wrapper, k6, and Infisical CLI versions using the chosen version-manager file and documentation.

## Acceptance Criteria

- Java is pinned to 25 and Node.js to one explicit supported release.
- k6 and Infisical CLI versions are explicit rather than `latest`.
- The Gradle version is owned by the wrapper.
- Testing: a clean version-manager invocation resolves all declared tools or reports a clear installation command.
