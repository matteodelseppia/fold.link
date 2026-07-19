# MVP-113: Add structured application logging

## Description

Configure parseable production logs for startup, creation outcomes, redirects, validation failures, and storage failures without sensitive destinations or credentials.

## Acceptance Criteria

- Logs include timestamp, level, event name, environment, and safe correlation identifier.
- Full destination URLs, query strings, Redis credentials, and secret values are excluded.
- Expected 404s do not emit stack traces.
- Testing: log-capture tests assert required fields and absence of representative secret/destination fixtures.
