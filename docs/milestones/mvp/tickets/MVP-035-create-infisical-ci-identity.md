# MVP-035: Create the Infisical CI identity

## Description

Create a least-privilege machine identity for GitLab deployment jobs to retrieve only deployment-related secrets.

## Acceptance Criteria

- The identity cannot read application Redis credentials unless a test job explicitly requires a scoped test path.
- Protected production access is separated from merge-request jobs.
- Authentication method and expiry are documented without storing credentials.
- Testing: a merge-request context cannot retrieve production deployment secrets; a protected main job can.
