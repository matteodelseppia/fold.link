# MVP-069: Add the disposable Redis test fixture

## Description

Provide a repeatable Redis integration-test fixture for local and CI use with unique namespaces and deterministic cleanup.

## Acceptance Criteria

- Tests never use developer, staging, or production Redis.
- The fixture waits for readiness and exposes connection data programmatically.
- Parallel test runs cannot share keys.
- Testing: run two integration-test processes concurrently and verify isolation and cleanup.
