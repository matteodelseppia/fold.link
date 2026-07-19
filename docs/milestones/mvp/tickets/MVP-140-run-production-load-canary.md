# MVP-140: Run a bounded production load canary

## Description

Run a deliberately small, pre-approved read-heavy canary to detect production-only latency or error regressions without stress testing the service.

## Acceptance Criteria

- Traffic volume and duration are below documented safe limits.
- The test targets only pre-created harmless aliases and does not follow external redirects.
- Stop conditions for errors, latency, and provider resource pressure are explicit.
- Testing: the canary meets NF03 thresholds and production alerts/metrics show expected traffic without saturation.
