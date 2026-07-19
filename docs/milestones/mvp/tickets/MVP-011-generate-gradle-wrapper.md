# MVP-011: Generate the Gradle wrapper

## Description
Commit a pinned Gradle wrapper so local and CI builds use the same build tool without a system Gradle dependency.

## Acceptance Criteria
- Standard Unix, Windows, properties, and wrapper JAR files are committed.
- Wrapper distribution uses HTTPS and checksum verification.
- Wrapper scripts retain executable permissions where applicable.
- Testing: `./gradlew --version` succeeds and reports Java 25 compatibility and the pinned Gradle version.
