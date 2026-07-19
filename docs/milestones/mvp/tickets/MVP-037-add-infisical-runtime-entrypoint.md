# MVP-037: Add the Infisical runtime entrypoint

## Description

Add an entrypoint that authenticates from Railway bootstrap variables, injects Infisical secrets into the Java process, and preserves signals and exit codes.

## Acceptance Criteria

- Java is the final executed process and receives termination signals.
- Secret values are not printed, persisted, or included in error messages.
- Missing authentication or required secrets stops startup with a clear redacted error.
- Testing: integration tests cover successful injection, missing credentials, and SIGTERM propagation.
