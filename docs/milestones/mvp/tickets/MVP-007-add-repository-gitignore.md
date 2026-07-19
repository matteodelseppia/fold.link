# MVP-007: Add repository ignore rules

## Description

Add ignore rules for Gradle, Java, IDE, Node test output, k6 output, operating-system files, local environment files, and Infisical artifacts.

## Acceptance Criteria

- Build outputs, `.env*` except the safe example, secrets, and IDE metadata are ignored.
- Source, Gradle wrapper files, test fixtures, and documentation remain trackable.
- No currently tracked planning document is ignored.
- Testing: create representative disposable files and verify `git status --ignored` classifies each correctly.
