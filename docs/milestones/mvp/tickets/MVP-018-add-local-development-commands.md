# MVP-018: Add local development commands

## Description
Add small, documented commands for starting Redis, running the application through Infisical, executing tests, and stopping local dependencies.

## Acceptance Criteria
- Commands use repository-pinned tools and do not print secret values.
- Start commands wait for Redis health or fail clearly.
- Stop commands preserve the named development volume by default.
- Testing: follow the commands from a fresh shell through start, test, and stop successfully.
