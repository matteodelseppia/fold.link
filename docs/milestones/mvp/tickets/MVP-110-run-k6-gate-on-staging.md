# MVP-110: Run the k6 gate on staging

## Description

Run the bounded read-heavy scenario against the candidate staging deployment before production promotion.

## Acceptance Criteria

- Target validation prevents accidental production traffic.
- Traffic volume respects Railway plan limits and uses disposable mappings.
- Threshold failure blocks promotion.
- Testing: complete one passing staging run and retain the k6 summary with deployed digest.
