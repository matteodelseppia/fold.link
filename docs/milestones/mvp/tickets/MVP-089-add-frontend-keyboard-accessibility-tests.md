# MVP-089: Add frontend keyboard accessibility tests

## Description
Verify the complete create-and-copy flow is operable with keyboard focus and correctly announced state changes.

## Acceptance Criteria
- Tab order follows input, submit, result link, and copy control.
- Enter submits the form once and focus is not trapped.
- Loading, error, and copied messages use appropriate live-region semantics.
- Testing: automated DOM accessibility assertions cover labels, focus order, activation, and announcement attributes.
