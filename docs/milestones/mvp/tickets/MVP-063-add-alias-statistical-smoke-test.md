# MVP-063: Add an alias statistical smoke test

## Description
Add a bounded test that generates a representative alias sample and catches obvious constant, alphabet, or length regressions without claiming cryptographic proof.

## Acceptance Criteria
- The sample size and non-flaky thresholds are documented.
- All generated aliases match the contract.
- The test fails for a constant-output fake generator.
- Testing: run repeatedly in CI-equivalent conditions and demonstrate stable results.
