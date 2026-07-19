# MVP-071: Implement the link-creation service

## Description

Implement the application service that validates and canonicalizes a destination, generates an alias, stores it, and returns a mapping.

## Acceptance Criteria

- Validation happens before alias generation or Redis access.
- The service depends only on domain ports and the alias generator.
- A successful result contains the stored canonical destination and alias.
- Testing: unit tests verify call order, success output, and zero repository calls for invalid input.
