# MVP-029: Add a container smoke-test script

## Description

Add a CI-friendly script that starts Redis and the application image, waits for readiness, checks one HTTP endpoint, and always cleans up.

## Acceptance Criteria

- The script has bounded retries and useful failure logs.
- Containers and networks use collision-safe names.
- Cleanup runs on success and failure without deleting persistent developer volumes.
- Testing: the script passes for the built image and fails within the timeout for an intentionally invalid image command.
