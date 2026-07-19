# MVP-013: Add the application entry point

## Description

Add the package structure and minimal Spring Boot main class for `fold.link`.

## Acceptance Criteria

- The entry point is in the root application package.
- The application starts without feature controllers when required local configuration is provided.
- Package naming is documented and consistent.
- Testing: a `@SpringBootTest` context-load test passes.
