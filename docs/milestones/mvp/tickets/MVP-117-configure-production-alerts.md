# MVP-117: Configure production alerts

## Description
Create actionable alerts for application unavailability, repeated restart, elevated 5xx/storage failure, and Redis resource pressure using supported hosted tooling.

## Acceptance Criteria
- Each alert has threshold, evaluation window, owner, and runbook link.
- Alerts avoid paging on expected 404 traffic.
- Delivery destination is verified without exposing user data.
- Testing: trigger or use provider test mode for each alert and confirm receipt and recovery notification.
