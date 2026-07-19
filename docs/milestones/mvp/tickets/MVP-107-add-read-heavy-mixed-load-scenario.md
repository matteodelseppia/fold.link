# MVP-107: Add the read-heavy mixed load scenario

## Description
Create the NF03 workload combining substantially more redirects than creations with an explicit ratio.

## Acceptance Criteria
- The configured redirect-to-create ratio is at least 20:1 and reported.
- Each operation has separate latency and error metrics.
- Load is bounded for CI and staging plans.
- Testing: a local run demonstrates the requested operation ratio within a documented tolerance.
