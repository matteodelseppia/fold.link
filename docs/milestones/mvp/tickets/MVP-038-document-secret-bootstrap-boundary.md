# MVP-038: Document the secret bootstrap boundary

## Description
Document the unavoidable Infisical machine-identity bootstrap variables stored in Railway and protected GitLab variables, including why they cannot themselves be fetched from Infisical.

## Acceptance Criteria
- Exact variable names and allowed locations are listed without values.
- Least privilege, masking, protection, rotation, and incident steps are stated.
- All other operational secrets are explicitly forbidden from those locations.
- Testing: audit both hosted platforms and verify only approved bootstrap variables cross the boundary.
