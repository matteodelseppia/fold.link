# MVP-091: Configure static resource serving

## Description

Serve the frontend HTML, CSS, and JavaScript from Spring Boot at `/` with correct media types and cache policy.

## Acceptance Criteria

- `/` returns the application page and assets return correct content types.
- HTML is revalidated while fingerprinting or conservative caching is defined for assets.
- Static paths do not conflict with `/{alias}`.
- Testing: packaged-application HTTP tests fetch the root and each asset and verify status, content type, cache headers, and routing.
