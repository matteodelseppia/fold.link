# MVP-015: Add typed runtime configuration

## Description

Bind the public base URL, alias settings, and Redis key prefix into validated Spring configuration properties.

## Acceptance Criteria

- Startup fails clearly when the public base URL is missing or invalid outside tests.
- Alias length and retry count accept only safe positive ranges.
- Defaults match the architecture decision record.
- Testing: configuration binding tests cover valid values, missing required values, and out-of-range values.
