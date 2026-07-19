# MVP-051: Add the staging deployment job

## Description
Add a protected `main` pipeline job that tells Railway staging to deploy the exact tested image digest.

## Acceptance Criteria
- The job consumes the digest artifact rather than reconstructing a tag.
- It waits for Railway rollout status with a bounded timeout.
- Failed rollout blocks all later stages and prints redacted diagnostics.
- Testing: deploy the skeleton image and verify the Railway deployment reports the same digest.
