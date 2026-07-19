# MVP-065: Configure the Spring Redis client

## Description
Configure Redis connectivity, timeouts, pooling policy, and serializers from injected environment values.

## Acceptance Criteria
- Connection and command timeouts are finite and documented.
- Keys and destinations use explicit UTF-8 string serialization.
- Secrets are redacted from startup and exception logs.
- Testing: an integration test connects to disposable Redis and a failure test confirms bounded timeout and redacted diagnostics.
