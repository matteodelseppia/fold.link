# MVP-092: Add the Node.js frontend test runner

## Description
Configure the built-in Node test runner and minimal DOM test dependency setup for static frontend unit tests.

## Acceptance Criteria
- Dependencies and lockfile are pinned.
- One command runs all frontend tests without a browser or global package.
- Test output can be emitted in GitLab-compatible format.
- Testing: run the suite twice from a clean install and obtain identical passing results.
