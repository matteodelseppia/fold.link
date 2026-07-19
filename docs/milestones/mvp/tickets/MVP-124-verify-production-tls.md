# MVP-124: Verify production TLS

## Description

Verify Railway has issued and serves a valid certificate for the production hostname and that HTTP behavior is safely upgraded to HTTPS.

## Acceptance Criteria

- Certificate hostname, issuer chain, and validity are correct.
- Plain HTTP redirects to HTTPS without losing the path.
- The application public base URL uses the canonical HTTPS hostname.
- Testing: automated TLS and HTTP checks pass from outside Railway and detect no certificate or redirect-loop errors.
