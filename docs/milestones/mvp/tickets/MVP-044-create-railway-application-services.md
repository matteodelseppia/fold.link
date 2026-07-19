# MVP-044: Create Railway application services

## Description
Create separate staging and production application services configured to deploy container images rather than build unreviewed source.

## Acceptance Criteria
- Each service belongs to the correct Railway environment.
- Service source is the GitLab container registry with no mutable default tag.
- The expected internal port and start command are configured.
- Testing: deploy a placeholder pinned image to staging and verify Railway starts the service; production remains unchanged.
