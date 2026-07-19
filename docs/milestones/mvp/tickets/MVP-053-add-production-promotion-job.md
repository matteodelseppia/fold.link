# MVP-053: Add the production promotion job

## Description

Add a protected promotion job that deploys the already-approved staging image digest to production without rebuilding it.

## Acceptance Criteria

- Promotion is unavailable to merge-request and unprotected branch pipelines.
- The exact staging-tested digest is used.
- The job supports the chosen approval policy and waits for healthy rollout.
- Testing: a dry run proves digest propagation; an unauthorized pipeline cannot invoke production deployment.
