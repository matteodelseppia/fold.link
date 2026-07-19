# MVP-112: Finalize pipeline promotion gates

## Description
Wire the full sequence: validate, test, build, container smoke, publish, staging deploy, staging system/load gates, and production promotion.

## Acceptance Criteria
- The DAG matches the design document and uses the same immutable digest after publication.
- Production cannot start before all staging gates pass.
- Deployment jobs are protected and serialized.
- Testing: GitLab CI lint passes and a pipeline graph review finds no bypass path to production.
