# MVP-073: Implement the link-lookup service

## Description
Implement the application service that validates a path alias and resolves it through the repository.

## Acceptance Criteria
- Invalid alias shapes are treated as not found and never queried.
- Repository hit, miss, and storage failure remain distinct outcomes.
- The service performs no URL reconstruction from request headers.
- Testing: unit tests cover valid hit, valid miss, invalid alias, and Redis failure.
