# MVP-026: Add a multi-stage application Dockerfile

## Description
Build a reproducible, non-root Java 25 container image using the Gradle wrapper and the executable JAR.

## Acceptance Criteria
- Build and runtime base images are pinned by digest.
- The runtime image contains no Gradle cache or source tree.
- The process runs as a non-root user and exposes the configured port.
- Testing: build the image and verify its user is non-root and its application process starts.
