# MVP-039: Create Railway project environments

## Description
Create one Railway project with isolated staging and production environments and record their non-secret identifiers.

## Acceptance Criteria
- Staging and production are clearly named and isolated.
- Team access follows least privilege and production changes are restricted.
- Project and environment identifiers are available to deployment automation.
- Testing: list resources in each environment and confirm no service or variable is unintentionally shared.
