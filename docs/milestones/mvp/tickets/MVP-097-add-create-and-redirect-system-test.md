# MVP-097: Add the create-and-redirect system test

## Description

Test the primary user journey through real HTTP and Redis: create a short URL, then request it and inspect the redirect.

## Acceptance Criteria

- Creation returns a contract-valid unique alias and absolute short URL.
- Following the short path returns `302` to the exact original destination.
- The test identifies coverage of F01 and F02.
- Testing: run against the packaged application and staging using a harmless controlled destination.
