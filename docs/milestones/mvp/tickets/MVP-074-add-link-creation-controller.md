# MVP-074: Add the link-creation controller

## Description

Expose `POST /api/links` and translate a valid request into the recorded success response.

## Acceptance Criteria

- Successful creation returns the decided status, JSON media type, and exact response fields.
- The absolute short URL uses configured public origin plus encoded alias.
- Request headers cannot override the public origin.
- Testing: MVC tests verify status, headers, JSON, service arguments, and spoofed forwarded-host behavior.
