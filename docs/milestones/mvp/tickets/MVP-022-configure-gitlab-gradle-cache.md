# MVP-022: Configure the GitLab Gradle cache

## Description

Add safe Gradle dependency caching to reduce pipeline duration without caching build outputs or credentials.

## Acceptance Criteria

- Cache keys change when Gradle lock or wrapper files change.
- The project-local Gradle cache path is used.
- Cache policy prevents untrusted branches from poisoning the protected-branch cache.
- Testing: two identical pipelines show a cache miss then a cache hit while producing the same test result.
