# MVP-014: Add base application configuration

## Description

Create base, local, test, staging, and production configuration files with safe defaults and environment-variable placeholders.

## Acceptance Criteria

- No credential or hosted endpoint is committed.
- Server port, public base URL, Redis connection, logging, and actuator settings have named variables.
- Test configuration cannot accidentally connect to production.
- Testing: start the app under each profile with fixture variables and verify configuration binding succeeds.
