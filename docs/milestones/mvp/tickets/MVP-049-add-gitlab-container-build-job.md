# MVP-049: Add the container build job

## Description
Add a GitLab job that builds the tested application image after the container smoke gate.

## Acceptance Criteria
- The build uses the committed Dockerfile and immutable source revision.
- Build metadata records the Git commit and build timestamp without secrets.
- Merge-request images are not pushed to a production-consumed tag.
- Testing: inspect the built image labels and run the image smoke test against it.
