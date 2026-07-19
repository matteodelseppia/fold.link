# MVP-054: Serialize environment deployments

## Description
Configure GitLab resource groups and cancellation rules so two pipelines cannot race deployments or promote an obsolete image.

## Acceptance Criteria
- Staging and production each allow one active deployment.
- A superseded pipeline cannot promote after a newer commit becomes authoritative.
- Build/test jobs remain parallel where safe.
- Testing: trigger two controlled pipelines and verify environment deployment order and final digest.
