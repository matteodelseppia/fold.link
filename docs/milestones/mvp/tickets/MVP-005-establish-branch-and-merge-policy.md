# MVP-005: Establish the branch and merge policy

## Description
Configure and document the GitLab workflow used to deliver the MVP safely through small merge requests.

## Acceptance Criteria
- `main` is protected from direct pushes and requires a successful pipeline.
- Merge requests use squash merging and delete source branches after merge.
- The policy documents emergency rollback commits and who may promote production.
- Testing: attempt or use GitLab's permission view to verify an unprivileged direct push cannot bypass the pipeline.
