# MVP-094: Add basic security response headers

## Description
Configure safe browser response headers for the static page, JSON API, errors, and redirects.

## Acceptance Criteria
- Content sniffing, framing, referrer, and a restrictive MVP content-security policy are configured.
- The CSP permits only the committed local assets and required form/fetch behavior.
- Redirect `Location` behavior remains intact.
- Testing: HTTP tests assert headers on `/`, an API response, a 404, and a redirect; the page loads without CSP violations.
