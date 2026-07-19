# MVP-123: Configure the production domain and DNS

## Description
Attach the intended fold.link production hostname to Railway and create the required DNS records with documented ownership.

## Acceptance Criteria
- DNS points only to the production application service.
- No staging service is reachable through the production hostname.
- TTL and rollback record changes are documented.
- Testing: query authoritative and public resolvers and verify the hostname resolves to Railway's expected target.
