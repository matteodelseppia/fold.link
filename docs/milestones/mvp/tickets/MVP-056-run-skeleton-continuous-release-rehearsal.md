# MVP-056: Rehearse skeleton continuous release

## Description

Run the complete main pipeline for the healthy skeleton through build, registry, staging, staging smoke, and controlled production promotion.

## Acceptance Criteria

- One immutable digest traverses every environment.
- All job durations, evidence links, and manual steps are recorded.
- Production returns the expected skeleton health response.
- Testing: execute the rollback job in staging and redeploy the candidate to prove both paths before feature development continues.
