# MVP-032: Define the Infisical secret schema

## Description
Document the canonical Infisical paths and variable names for Redis, the public base URL, Railway deployment access, and runtime bootstrap as the primary implementation control for NF02.

## Acceptance Criteria
- Names map one-to-one to Spring and pipeline configuration.
- Secret values, credentials, and tokens are never documented.
- Ownership and rotation expectation are stated for each secret class.
- The schema and bootstrap exception together account for every secret-bearing configuration required by NF02.
- Testing: a schema-validation script or checklist detects an omitted required name and an unexpected duplicate alias.
