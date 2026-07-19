# MVP-050: Publish immutable registry images

## Description

Push successful `main` images to the GitLab registry using the full commit SHA and digest as immutable release identities.

## Acceptance Criteria

- No deployment consumes `latest` or another mutable branch tag.
- The pipeline records the pushed digest as a downstream artifact.
- Registry authentication uses masked, protected variables.
- Testing: pull by recorded digest and verify its commit label matches the pipeline SHA.
