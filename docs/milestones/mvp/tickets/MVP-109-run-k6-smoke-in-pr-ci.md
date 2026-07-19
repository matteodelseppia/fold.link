# MVP-109: Run k6 smoke testing in merge-request CI

## Description

Add a short k6 mixed-workload smoke job against the disposable CI application.

## Acceptance Criteria

- The pinned k6 version is used.
- The job runs after functional system tests and blocks merge on threshold failure.
- Summary output is retained as an artifact.
- Testing: a deliberate redirect error or impossible branch threshold fails the pipeline.
