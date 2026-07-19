# MVP-040: Provision staging Redis

## Description

Provision the Railway Redis service used only by staging and capture its connection values in Infisical staging.

## Acceptance Criteria

- The service is attached only to the staging environment.
- TLS/authentication is enabled when supported and credentials live only in Infisical.
- Application-compatible Redis URL variables are populated.
- Testing: a temporary authorized client can ping and round-trip a key; production credentials cannot access it.
