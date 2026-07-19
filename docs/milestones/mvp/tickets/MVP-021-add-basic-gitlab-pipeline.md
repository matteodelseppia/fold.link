# MVP-021: Add the basic GitLab pipeline

## Description
Create the initial `.gitlab-ci.yml` with validate and test stages so every subsequent merge request receives automated feedback.

## Acceptance Criteria
- Pipelines run for merge requests and `main`, and are interruptible on superseding commits.
- The first jobs run Markdown lint, formatting check, and the context-load test.
- Job images are pinned and no secrets are required.
- Testing: a branch pipeline passes; a deliberate lint failure makes the pipeline fail.
