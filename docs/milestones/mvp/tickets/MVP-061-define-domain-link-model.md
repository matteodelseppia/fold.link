# MVP-061: Define the URL mapping domain model

## Description
Add the immutable domain type representing an alias and canonical destination independently of Spring MVC and Redis DTOs.

## Acceptance Criteria
- Construction enforces nonblank alias and destination invariants.
- The model contains no framework persistence annotations.
- Equality behavior supports deterministic tests.
- Testing: unit tests cover valid construction and each invariant failure.
