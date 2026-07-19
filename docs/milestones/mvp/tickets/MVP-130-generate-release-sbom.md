# MVP-130: Generate the release SBOM

## Description
Generate a machine-readable software bill of materials for application dependencies and container packages for each main image.

## Acceptance Criteria
- The SBOM is tied to commit SHA and image digest.
- It is retained as a pipeline artifact and contains no credentials.
- Format and retrieval instructions are documented.
- Testing: validate the SBOM schema and confirm it includes the application JAR, Java runtime, and representative locked dependencies.
