# MVP-023: Publish unit-test reports

## Description

Configure GitLab to retain JUnit XML and diagnostic test artifacts for every test job.

## Acceptance Criteria

- Reports are uploaded even when tests fail.
- Artifacts have a finite documented retention period.
- The merge request UI displays test counts and failures.
- Testing: commit a temporary failing test on a branch and verify its failure appears in the GitLab test report.
