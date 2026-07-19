# MVP-105: Add the k6 redirect load scenario

## Description

Create a read-focused k6 scenario that pre-creates mappings, requests aliases without following redirects, and validates status and `Location`.

## Acceptance Criteria

- Setup traffic is excluded from redirect measurements.
- The scenario uses several aliases to avoid a single-key artifact.
- Checks fail on wrong status or destination.
- Testing: run locally and observe nonzero redirect throughput with zero check failures.
