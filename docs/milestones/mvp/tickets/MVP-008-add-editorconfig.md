# MVP-008: Add EditorConfig rules

## Description

Add repository-wide whitespace, encoding, newline, and indentation rules for Java, Gradle, YAML, JSON, Markdown, HTML, CSS, and JavaScript.

## Acceptance Criteria

- UTF-8, LF, final newline, and trailing-whitespace rules are defined.
- Java/Gradle use four spaces and web/YAML files use two spaces.
- Markdown preserves intentional trailing spaces.
- Testing: run the selected formatter or EditorConfig checker against a deliberately misformatted fixture and confirm it reports the violation.
