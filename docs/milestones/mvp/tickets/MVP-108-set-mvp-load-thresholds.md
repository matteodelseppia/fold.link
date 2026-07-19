# MVP-108: Set MVP load-test thresholds

## Description

Record pragmatic pass/fail thresholds for redirect latency, creation latency, HTTP failure rate, and check success under the bounded MVP workload.

## Acceptance Criteria

- Thresholds are numeric, environment-aware, and tied to NF03.
- Redirect thresholds are evaluated separately from creation.
- The rationale and permitted future revision process are documented.
- Testing: a healthy local run passes and an intentionally impossible threshold makes k6 exit nonzero.
