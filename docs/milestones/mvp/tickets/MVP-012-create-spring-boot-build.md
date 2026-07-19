# MVP-012: Create the Spring Boot build definition

## Description

Create the minimal Gradle settings and build files for a Java 25 Spring Boot 4 application.

## Acceptance Criteria

- Plugins and dependencies are pinned through a single version source.
- Web, validation, Redis, actuator, and test dependencies are declared.
- Java toolchains require Java 25 and tests use JUnit Platform.
- Testing: `./gradlew dependencies` and `./gradlew tasks` complete without resolution errors.
