# MVP-024: Add the application build job

## Description
Add a pipeline job that produces the executable Spring Boot JAR only after validation and unit tests pass.

## Acceptance Criteria
- The job uses the wrapper and locked dependencies.
- Exactly one runnable application JAR is retained as an artifact.
- The job does not run after an earlier gate fails.
- Testing: download the artifact and start it with fixture configuration; the context becomes healthy.
