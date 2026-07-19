# MVP-090: Check frontend color and contrast

## Description
Validate text, controls, focus indicators, and state messages against the chosen WCAG AA contrast targets.

## Acceptance Criteria
- Normal text, large text, and component boundaries meet their documented ratios.
- Error and success meaning is not conveyed by color alone.
- Any exception is removed rather than deferred from MVP.
- Testing: run an automated accessibility scanner on the rendered page and retain a zero-critical-violation report.
