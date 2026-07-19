# MVP-104: Create the k6 test configuration

## Description
Add shared k6 configuration for target URL, generated test data, thresholds, safety limits, and local versus staging execution.

## Acceptance Criteria
- Defaults cannot target production.
- Duration, virtual users, and thresholds are overrideable only within documented safety bounds.
- Test output includes request rate, latency, and error rate.
- Testing: a one-iteration local smoke run passes and an absent target fails safely before traffic is sent.
