# MVP-139: Verify production mapping across application redeploy

## Description

Verify NF01 at production application level by checking a smoke-test alias before and after a controlled redeploy of the same immutable digest.

## Acceptance Criteria

- The same alias redirects correctly before and after redeploy.
- Redis is not restarted or mutated beyond normal lookup during this check.
- Railway reports the same digest and healthy replacement instance.
- Testing: automated redirect assertions pass at both checkpoints and are linked to NF01 evidence.
