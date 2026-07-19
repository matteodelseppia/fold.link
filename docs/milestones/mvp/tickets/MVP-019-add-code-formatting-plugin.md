# MVP-019: Add deterministic code formatting

## Description

Configure a pinned Gradle formatter for Java and Gradle sources with separate check and apply tasks.

## Acceptance Criteria

- Formatting rules are deterministic on Java 25.
- `check` runs the non-mutating format check.
- Generated/build directories are excluded.
- Testing: formatting check passes, then detects a temporary deliberately misformatted source file.
