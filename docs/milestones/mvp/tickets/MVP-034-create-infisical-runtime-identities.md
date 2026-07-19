# MVP-034: Create Infisical runtime identities

## Description
Create separate machine identities for staging and production runtime secret retrieval.

## Acceptance Criteria
- Each identity can read only its own environment and required secret path.
- Neither identity has write or administrative permission.
- Credential lifetime and rotation owner are documented.
- Testing: authenticate as each identity, read its path successfully, and confirm cross-environment reads are denied.
