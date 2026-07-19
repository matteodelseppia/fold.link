# MVP-078: Add the missing-alias response

## Description

Return a clear `404 Not Found` response when the redirect route receives an unknown or invalid alias.

## Acceptance Criteria

- Unknown and syntactically invalid aliases both return 404.
- Redis is not queried for invalid alias shapes.
- API routes and static assets are not swallowed by the catch-all alias route.
- Testing: MVC tests cover unknown, too-short, illegal-character aliases and routing precedence.
