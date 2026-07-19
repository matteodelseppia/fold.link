# MVP-102: Run system tests in merge-request CI

## Description
Add a disposable Redis and packaged-application system-test job to merge-request pipelines.

## Acceptance Criteria
- The job depends on unit/frontend checks and the built JAR.
- It uses isolated ephemeral data and no hosted environment secrets.
- JUnit-compatible results and process logs are retained.
- Testing: a deliberate redirect regression fails the job and blocks merge.
