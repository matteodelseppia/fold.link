# MVP-057: Define API request and response DTOs

## Description

Implement immutable request and response records for link creation using the contract in the architecture decision record.

## Acceptance Criteria

- The request contains exactly the destination URL field.
- The success response contains alias, original URL, and absolute short URL.
- JSON property names and nullability are explicit.
- Testing: serialization tests assert the exact success JSON and reject unknown contract drift as configured.
