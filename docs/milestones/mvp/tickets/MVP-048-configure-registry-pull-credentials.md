# MVP-048: Configure Railway registry pull access

## Description
Create read-only GitLab registry credentials and attach them to Railway without granting repository write or API administration rights.

## Acceptance Criteria
- Credentials can pull only the required project image.
- Credentials are stored as hosted secrets and documented in the rotation inventory.
- Staging and production credential reuse is explicitly accepted or separated.
- Testing: Railway pulls a private pinned image successfully; an attempted registry push with the credential fails.
