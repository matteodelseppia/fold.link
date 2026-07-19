# MVP-106: Add the k6 creation load scenario

## Description

Create a bounded URL-creation scenario that validates response contract, alias shape, and uniqueness within the observed sample.

## Acceptance Criteria

- Destinations are valid controlled URLs with unique query values.
- Responses are checked for status and schema.
- Observed duplicate aliases fail the test while acknowledging this is not a proof of uniqueness.
- Testing: run locally and verify all created aliases resolve after the scenario.
