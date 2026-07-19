# MVP-087: Add copy-to-clipboard behavior

## Description
Add a copy control for the generated short URL with accessible success and failure feedback.

## Acceptance Criteria
- The control is disabled until a short URL exists.
- Clipboard permission rejection preserves the selectable URL and shows fallback guidance.
- Copy status is announced without stealing focus.
- Testing: Node tests mock successful and rejected Clipboard API calls and assert both states.
