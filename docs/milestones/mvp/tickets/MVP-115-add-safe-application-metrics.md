# MVP-115: Add safe application metrics

## Description
Expose counters and timers for create outcomes, redirect hit/miss/failure, and Redis latency without alias or destination labels.

## Acceptance Criteria
- Metric names, units, outcome labels, and cardinality are documented.
- No user-provided string becomes a metric tag.
- The metrics endpoint is restricted from public internet access or disabled if Railway cannot protect it.
- Testing: integration tests perform requests and assert expected metric deltas and bounded tag sets.
