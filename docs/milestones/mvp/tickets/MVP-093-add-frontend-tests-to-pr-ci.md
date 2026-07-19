# MVP-093: Add frontend tests to merge-request CI

## Description
Add a GitLab job for locked Node dependency installation, frontend unit tests, and report publication.

## Acceptance Criteria
- The job runs for merge requests before build/deploy stages.
- Node cache keys include the lockfile.
- Test failures appear in the merge-request report and block merge.
- Testing: a deliberate failing frontend test blocks a branch pipeline and publishes its failure.
