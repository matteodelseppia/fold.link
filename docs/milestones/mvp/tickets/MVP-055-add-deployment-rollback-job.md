# MVP-055: Add the deployment rollback job

## Description

Add a protected manual job that redeploys a previously known-good immutable digest to the selected Railway environment.

## Acceptance Criteria

- The operator supplies or selects an existing registry digest.
- The job validates the image belongs to this project before deployment.
- Rollback is logged as a GitLab deployment and never rebuilds source.
- Testing: roll staging back between two skeleton digests and verify health and reported digest.
