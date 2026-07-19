# MVP-016: Add a local Redis Compose service

## Description
Add a Docker Compose definition for a pinned Redis image with a named persistence volume and a health check.

## Acceptance Criteria
- Redis is reachable only through the documented local port.
- AOF persistence is enabled for the development service.
- The service reports healthy before application startup instructions proceed.
- Testing: start Compose, write a key, recreate the container without deleting the volume, and verify the key remains.
