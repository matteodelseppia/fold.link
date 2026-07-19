# MVP-137: Promote the release candidate to production

## Description
Use the protected pipeline job to deploy the exact staging-approved image digest to Railway production.

## Acceptance Criteria
- The production deployment digest exactly matches the staging acceptance digest.
- Railway reports a healthy rollout within the bounded timeout.
- Deployment actor, pipeline, digest, start/end time, and approval are recorded.
- Testing: the pipeline's immediate production health check passes; no manual source build or mutable tag is used.
