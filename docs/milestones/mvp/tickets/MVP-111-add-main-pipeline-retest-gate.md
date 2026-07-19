# MVP-111: Add the main-pipeline retest gate

## Description

Ensure every merge to `main` rebuilds and reruns unit, frontend, system, and CI load tests before image publication.

## Acceptance Criteria

- Main does not reuse untrusted merge-request build outputs.
- All required test jobs precede registry publication.
- A failure in any suite stops release.
- Testing: inspect the pipeline DAG and demonstrate one failing suite prevents the publish job.
