# MVP-116: Configure Railway observability views

## Description
Configure saved log/metric views for deploy health, HTTP 5xx, Redis failures, restart loops, and latency using available Railway facilities.

## Acceptance Criteria
- Staging and production views are clearly separated.
- Filters use structured events and never display secret values.
- Operator links are added to the runbook.
- Testing: generate a controlled staging validation error and storage failure and confirm each appears in the intended view.
