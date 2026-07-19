# MVP Ticket Execution Index

Generated from every file in `docs/milestones/mvp/tickets/`. Predecessors are the minimum
set of tickets that must merge first for the listed ticket to be buildable; `none` means it
can start immediately (subject to its own phase being reachable).

## Key milestones

- **First continuously deployable skeleton:** [MVP-030](tickets/MVP-030-run-container-smoke-test-in-ci.md) (basic pipeline + Dockerfile + healthcheck + CI-run smoke test all in place).
- **Production-release gate:** [MVP-136](tickets/MVP-136-perform-production-release-preflight.md) (preflight sign-off; [MVP-137](tickets/MVP-137-promote-release-candidate-to-production.md) executes the promotion it gates).

## Index

| # | Ticket | Phase | Predecessors | Status |
| --- | -------- | ------- | --------------- | -------- |
| 1 | [MVP-001](tickets/MVP-001-record-mvp-technical-decisions.md) | Foundational Decisions | none | Closed |
| 3 | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Repository Governance & Hygiene | [MVP-001](tickets/MVP-001-record-mvp-technical-decisions.md) | Open |
| 4 | [MVP-004](tickets/MVP-004-define-done-and-evidence-rules.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 5 | [MVP-005](tickets/MVP-005-establish-branch-and-merge-policy.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 6 | [MVP-006](tickets/MVP-006-replace-template-readme.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 7 | [MVP-007](tickets/MVP-007-add-repository-gitignore.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 8 | [MVP-008](tickets/MVP-008-add-editorconfig.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 9 | [MVP-009](tickets/MVP-009-pin-local-tool-versions.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 10 | [MVP-010](tickets/MVP-010-add-markdown-lint-configuration.md) | Repository Governance & Hygiene | [MVP-003](tickets/MVP-003-create-ticket-execution-index.md) | Open |
| 11 | [MVP-011](tickets/MVP-011-generate-gradle-wrapper.md) | Build, Container & CI Skeleton | [MVP-010](tickets/MVP-010-add-markdown-lint-configuration.md) | Open |
| 12 | [MVP-012](tickets/MVP-012-create-spring-boot-build.md) | Build, Container & CI Skeleton | [MVP-011](tickets/MVP-011-generate-gradle-wrapper.md) | Open |
| 13 | [MVP-013](tickets/MVP-013-add-application-entrypoint.md) | Build, Container & CI Skeleton | [MVP-012](tickets/MVP-012-create-spring-boot-build.md) | Open |
| 14 | [MVP-014](tickets/MVP-014-add-base-application-configuration.md) | Build, Container & CI Skeleton | [MVP-013](tickets/MVP-013-add-application-entrypoint.md) | Open |
| 15 | [MVP-015](tickets/MVP-015-add-typed-runtime-configuration.md) | Build, Container & CI Skeleton | [MVP-014](tickets/MVP-014-add-base-application-configuration.md) | Open |
| 16 | [MVP-016](tickets/MVP-016-add-local-redis-compose-service.md) | Build, Container & CI Skeleton | [MVP-015](tickets/MVP-015-add-typed-runtime-configuration.md) | Open |
| 17 | [MVP-017](tickets/MVP-017-add-safe-local-environment-example.md) | Build, Container & CI Skeleton | [MVP-016](tickets/MVP-016-add-local-redis-compose-service.md) | Open |
| 18 | [MVP-018](tickets/MVP-018-add-local-development-commands.md) | Build, Container & CI Skeleton | [MVP-017](tickets/MVP-017-add-safe-local-environment-example.md) | Open |
| 19 | [MVP-019](tickets/MVP-019-add-code-formatting-plugin.md) | Build, Container & CI Skeleton | [MVP-018](tickets/MVP-018-add-local-development-commands.md) | Open |
| 20 | [MVP-020](tickets/MVP-020-enable-gradle-dependency-locking.md) | Build, Container & CI Skeleton | [MVP-019](tickets/MVP-019-add-code-formatting-plugin.md) | Open |
| 21 | [MVP-021](tickets/MVP-021-add-basic-gitlab-pipeline.md) | Build, Container & CI Skeleton | [MVP-020](tickets/MVP-020-enable-gradle-dependency-locking.md) | Open |
| 22 | [MVP-022](tickets/MVP-022-configure-gitlab-gradle-cache.md) | Build, Container & CI Skeleton | [MVP-021](tickets/MVP-021-add-basic-gitlab-pipeline.md) | Open |
| 23 | [MVP-023](tickets/MVP-023-publish-unit-test-reports.md) | Build, Container & CI Skeleton | [MVP-022](tickets/MVP-022-configure-gitlab-gradle-cache.md) | Open |
| 24 | [MVP-024](tickets/MVP-024-add-build-artifact-job.md) | Build, Container & CI Skeleton | [MVP-023](tickets/MVP-023-publish-unit-test-reports.md) | Open |
| 25 | [MVP-025](tickets/MVP-025-add-actuator-health-endpoint.md) | Build, Container & CI Skeleton | [MVP-024](tickets/MVP-024-add-build-artifact-job.md) | Open |
| 26 | [MVP-026](tickets/MVP-026-add-multi-stage-dockerfile.md) | Build, Container & CI Skeleton | [MVP-025](tickets/MVP-025-add-actuator-health-endpoint.md) | Open |
| 27 | [MVP-027](tickets/MVP-027-add-docker-build-context-rules.md) | Build, Container & CI Skeleton | [MVP-026](tickets/MVP-026-add-multi-stage-dockerfile.md) | Open |
| 28 | [MVP-028](tickets/MVP-028-add-container-healthcheck.md) | Build, Container & CI Skeleton | [MVP-027](tickets/MVP-027-add-docker-build-context-rules.md) | Open |
| 29 | [MVP-029](tickets/MVP-029-add-container-smoke-test-script.md) | Build, Container & CI Skeleton | [MVP-028](tickets/MVP-028-add-container-healthcheck.md) | Open |
| 30 | [MVP-030](tickets/MVP-030-run-container-smoke-test-in-ci.md) | Build, Container & CI Skeleton | [MVP-029](tickets/MVP-029-add-container-smoke-test-script.md) | Open |
| 31 | [MVP-031](tickets/MVP-031-create-infisical-project-and-environments.md) | Secrets Management (Infisical) | [MVP-030](tickets/MVP-030-run-container-smoke-test-in-ci.md) | Open |
| 32 | [MVP-032](tickets/MVP-032-define-infisical-secret-schema.md) | Secrets Management (Infisical) | [MVP-031](tickets/MVP-031-create-infisical-project-and-environments.md) | Open |
| 33 | [MVP-033](tickets/MVP-033-populate-development-secrets.md) | Secrets Management (Infisical) | [MVP-032](tickets/MVP-032-define-infisical-secret-schema.md) | Open |
| 34 | [MVP-034](tickets/MVP-034-create-infisical-runtime-identities.md) | Secrets Management (Infisical) | [MVP-033](tickets/MVP-033-populate-development-secrets.md) | Open |
| 35 | [MVP-035](tickets/MVP-035-create-infisical-ci-identity.md) | Secrets Management (Infisical) | [MVP-034](tickets/MVP-034-create-infisical-runtime-identities.md) | Open |
| 36 | [MVP-036](tickets/MVP-036-add-infisical-cli-to-runtime-image.md) | Secrets Management (Infisical) | [MVP-035](tickets/MVP-035-create-infisical-ci-identity.md) | Open |
| 37 | [MVP-037](tickets/MVP-037-add-infisical-runtime-entrypoint.md) | Secrets Management (Infisical) | [MVP-036](tickets/MVP-036-add-infisical-cli-to-runtime-image.md) | Open |
| 38 | [MVP-038](tickets/MVP-038-document-secret-bootstrap-boundary.md) | Secrets Management (Infisical) | [MVP-037](tickets/MVP-037-add-infisical-runtime-entrypoint.md) | Open |
| 39 | [MVP-039](tickets/MVP-039-create-railway-project-environments.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-038](tickets/MVP-038-document-secret-bootstrap-boundary.md) | Open |
| 40 | [MVP-040](tickets/MVP-040-provision-staging-redis.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-039](tickets/MVP-039-create-railway-project-environments.md) | Open |
| 41 | [MVP-041](tickets/MVP-041-configure-staging-redis-persistence.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-040](tickets/MVP-040-provision-staging-redis.md) | Open |
| 42 | [MVP-042](tickets/MVP-042-provision-production-redis.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-041](tickets/MVP-041-configure-staging-redis-persistence.md) | Open |
| 43 | [MVP-043](tickets/MVP-043-configure-production-redis-persistence.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-042](tickets/MVP-042-provision-production-redis.md) | Open |
| 44 | [MVP-044](tickets/MVP-044-create-railway-application-services.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-043](tickets/MVP-043-configure-production-redis-persistence.md) | Open |
| 45 | [MVP-045](tickets/MVP-045-configure-railway-health-and-restart-policy.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-044](tickets/MVP-044-create-railway-application-services.md) | Open |
| 46 | [MVP-046](tickets/MVP-046-configure-railway-bootstrap-variables.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-045](tickets/MVP-045-configure-railway-health-and-restart-policy.md) | Open |
| 47 | [MVP-047](tickets/MVP-047-configure-public-service-urls.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-046](tickets/MVP-046-configure-railway-bootstrap-variables.md) | Open |
| 48 | [MVP-048](tickets/MVP-048-configure-registry-pull-credentials.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-047](tickets/MVP-047-configure-public-service-urls.md) | Open |
| 49 | [MVP-049](tickets/MVP-049-add-gitlab-container-build-job.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-048](tickets/MVP-048-configure-registry-pull-credentials.md) | Open |
| 50 | [MVP-050](tickets/MVP-050-publish-immutable-registry-image.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-049](tickets/MVP-049-add-gitlab-container-build-job.md) | Open |
| 51 | [MVP-051](tickets/MVP-051-add-staging-deployment-job.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-050](tickets/MVP-050-publish-immutable-registry-image.md) | Open |
| 52 | [MVP-052](tickets/MVP-052-add-staging-health-smoke-job.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-051](tickets/MVP-051-add-staging-deployment-job.md) | Open |
| 53 | [MVP-053](tickets/MVP-053-add-production-promotion-job.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-052](tickets/MVP-052-add-staging-health-smoke-job.md) | Open |
| 54 | [MVP-054](tickets/MVP-054-add-deployment-serialization.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-053](tickets/MVP-053-add-production-promotion-job.md) | Open |
| 55 | [MVP-055](tickets/MVP-055-add-deployment-rollback-job.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-054](tickets/MVP-054-add-deployment-serialization.md) | Open |
| 56 | [MVP-056](tickets/MVP-056-run-skeleton-continuous-release-rehearsal.md) | Infrastructure Provisioning & Release Pipeline (Railway) | [MVP-055](tickets/MVP-055-add-deployment-rollback-job.md) | Open |
| 57 | [MVP-057](tickets/MVP-057-define-api-request-and-response-dtos.md) | Core Domain & Persistence | [MVP-056](tickets/MVP-056-run-skeleton-continuous-release-rehearsal.md) | Open |
| 58 | [MVP-058](tickets/MVP-058-implement-url-scheme-validation.md) | Core Domain & Persistence | [MVP-057](tickets/MVP-057-define-api-request-and-response-dtos.md) | Open |
| 59 | [MVP-059](tickets/MVP-059-implement-url-host-validation.md) | Core Domain & Persistence | [MVP-058](tickets/MVP-058-implement-url-scheme-validation.md) | Open |
| 60 | [MVP-060](tickets/MVP-060-implement-url-canonicalization.md) | Core Domain & Persistence | [MVP-059](tickets/MVP-059-implement-url-host-validation.md) | Open |
| 61 | [MVP-061](tickets/MVP-061-define-domain-link-model.md) | Core Domain & Persistence | [MVP-060](tickets/MVP-060-implement-url-canonicalization.md) | Open |
| 62 | [MVP-062](tickets/MVP-062-implement-secure-alias-generator.md) | Core Domain & Persistence | [MVP-061](tickets/MVP-061-define-domain-link-model.md) | Open |
| 63 | [MVP-063](tickets/MVP-063-add-alias-statistical-smoke-test.md) | Core Domain & Persistence | [MVP-062](tickets/MVP-062-implement-secure-alias-generator.md) | Open |
| 64 | [MVP-064](tickets/MVP-064-define-url-mapping-repository-port.md) | Core Domain & Persistence | [MVP-063](tickets/MVP-063-add-alias-statistical-smoke-test.md) | Open |
| 65 | [MVP-065](tickets/MVP-065-configure-spring-redis-client.md) | Core Domain & Persistence | [MVP-064](tickets/MVP-064-define-url-mapping-repository-port.md) | Open |
| 66 | [MVP-066](tickets/MVP-066-implement-versioned-redis-key-codec.md) | Core Domain & Persistence | [MVP-065](tickets/MVP-065-configure-spring-redis-client.md) | Open |
| 67 | [MVP-067](tickets/MVP-067-implement-atomic-redis-create.md) | Core Domain & Persistence | [MVP-066](tickets/MVP-066-implement-versioned-redis-key-codec.md) | Open |
| 68 | [MVP-068](tickets/MVP-068-implement-redis-alias-lookup.md) | Core Domain & Persistence | [MVP-067](tickets/MVP-067-implement-atomic-redis-create.md) | Open |
| 69 | [MVP-069](tickets/MVP-069-add-disposable-redis-test-fixture.md) | Core Domain & Persistence | [MVP-068](tickets/MVP-068-implement-redis-alias-lookup.md) | Open |
| 70 | [MVP-070](tickets/MVP-070-add-redis-repository-contract-tests.md) | Core Domain & Persistence | [MVP-069](tickets/MVP-069-add-disposable-redis-test-fixture.md) | Open |
| 71 | [MVP-071](tickets/MVP-071-implement-link-creation-service.md) | Core Domain & Persistence | [MVP-070](tickets/MVP-070-add-redis-repository-contract-tests.md) | Open |
| 72 | [MVP-072](tickets/MVP-072-implement-alias-collision-retry.md) | Core Domain & Persistence | [MVP-071](tickets/MVP-071-implement-link-creation-service.md) | Open |
| 73 | [MVP-073](tickets/MVP-073-implement-link-lookup-service.md) | Core Domain & Persistence | [MVP-072](tickets/MVP-072-implement-alias-collision-retry.md) | Open |
| 74 | [MVP-074](tickets/MVP-074-add-link-creation-controller.md) | API Layer | [MVP-073](tickets/MVP-073-implement-link-lookup-service.md) | Open |
| 75 | [MVP-075](tickets/MVP-075-add-validation-error-response.md) | API Layer | [MVP-074](tickets/MVP-074-add-link-creation-controller.md) | Open |
| 76 | [MVP-076](tickets/MVP-076-add-api-storage-failure-response.md) | API Layer | [MVP-075](tickets/MVP-075-add-validation-error-response.md) | Open |
| 77 | [MVP-077](tickets/MVP-077-add-redirect-controller.md) | API Layer | [MVP-076](tickets/MVP-076-add-api-storage-failure-response.md) | Open |
| 78 | [MVP-078](tickets/MVP-078-add-missing-alias-response.md) | API Layer | [MVP-077](tickets/MVP-077-add-redirect-controller.md) | Open |
| 79 | [MVP-079](tickets/MVP-079-add-redirect-storage-failure-response.md) | API Layer | [MVP-078](tickets/MVP-078-add-missing-alias-response.md) | Open |
| 80 | [MVP-080](tickets/MVP-080-add-api-content-and-size-limits.md) | API Layer | [MVP-079](tickets/MVP-079-add-redirect-storage-failure-response.md) | Open |
| 81 | [MVP-081](tickets/MVP-081-add-api-contract-test-suite.md) | API Layer | [MVP-080](tickets/MVP-080-add-api-content-and-size-limits.md) | Open |
| 82 | [MVP-082](tickets/MVP-082-add-frontend-html-shell.md) | Frontend | [MVP-081](tickets/MVP-081-add-api-contract-test-suite.md) | Open |
| 83 | [MVP-083](tickets/MVP-083-style-responsive-frontend.md) | Frontend | [MVP-082](tickets/MVP-082-add-frontend-html-shell.md) | Open |
| 84 | [MVP-084](tickets/MVP-084-implement-frontend-submit-flow.md) | Frontend | [MVP-083](tickets/MVP-083-style-responsive-frontend.md) | Open |
| 85 | [MVP-085](tickets/MVP-085-add-frontend-input-validation.md) | Frontend | [MVP-084](tickets/MVP-084-implement-frontend-submit-flow.md) | Open |
| 86 | [MVP-086](tickets/MVP-086-render-created-short-link.md) | Frontend | [MVP-085](tickets/MVP-085-add-frontend-input-validation.md) | Open |
| 87 | [MVP-087](tickets/MVP-087-add-copy-to-clipboard-action.md) | Frontend | [MVP-086](tickets/MVP-086-render-created-short-link.md) | Open |
| 88 | [MVP-088](tickets/MVP-088-render-frontend-api-errors.md) | Frontend | [MVP-087](tickets/MVP-087-add-copy-to-clipboard-action.md) | Open |
| 89 | [MVP-089](tickets/MVP-089-add-frontend-keyboard-accessibility-tests.md) | Frontend | [MVP-088](tickets/MVP-088-render-frontend-api-errors.md) | Open |
| 90 | [MVP-090](tickets/MVP-090-add-frontend-color-and-contrast-check.md) | Frontend | [MVP-089](tickets/MVP-089-add-frontend-keyboard-accessibility-tests.md) | Open |
| 91 | [MVP-091](tickets/MVP-091-configure-static-resource-serving.md) | Frontend | [MVP-090](tickets/MVP-090-add-frontend-color-and-contrast-check.md) | Open |
| 92 | [MVP-092](tickets/MVP-092-add-frontend-node-test-runner.md) | Frontend | [MVP-091](tickets/MVP-091-configure-static-resource-serving.md) | Open |
| 93 | [MVP-093](tickets/MVP-093-add-frontend-tests-to-pr-ci.md) | Frontend | [MVP-092](tickets/MVP-092-add-frontend-node-test-runner.md) | Open |
| 94 | [MVP-094](tickets/MVP-094-add-basic-security-response-headers.md) | Frontend | [MVP-093](tickets/MVP-093-add-frontend-tests-to-pr-ci.md) | Open |
| 95 | [MVP-095](tickets/MVP-095-add-cache-control-policy.md) | Frontend | [MVP-094](tickets/MVP-094-add-basic-security-response-headers.md) | Open |
| 96 | [MVP-096](tickets/MVP-096-create-system-test-harness.md) | System & Load Testing | [MVP-095](tickets/MVP-095-add-cache-control-policy.md) | Open |
| 97 | [MVP-097](tickets/MVP-097-add-create-and-redirect-system-test.md) | System & Load Testing | [MVP-096](tickets/MVP-096-create-system-test-harness.md) | Open |
| 98 | [MVP-098](tickets/MVP-098-add-unknown-alias-system-test.md) | System & Load Testing | [MVP-097](tickets/MVP-097-add-create-and-redirect-system-test.md) | Open |
| 99 | [MVP-099](tickets/MVP-099-add-invalid-url-system-tests.md) | System & Load Testing | [MVP-098](tickets/MVP-098-add-unknown-alias-system-test.md) | Open |
| 100 | [MVP-100](tickets/MVP-100-add-frontend-system-test.md) | System & Load Testing | [MVP-099](tickets/MVP-099-add-invalid-url-system-tests.md) | Open |
| 101 | [MVP-101](tickets/MVP-101-add-mapping-persistence-system-test.md) | System & Load Testing | [MVP-100](tickets/MVP-100-add-frontend-system-test.md) | Open |
| 102 | [MVP-102](tickets/MVP-102-run-system-tests-in-pr-ci.md) | System & Load Testing | [MVP-101](tickets/MVP-101-add-mapping-persistence-system-test.md) | Open |
| 103 | [MVP-103](tickets/MVP-103-run-system-tests-on-staging.md) | System & Load Testing | [MVP-102](tickets/MVP-102-run-system-tests-in-pr-ci.md) | Open |
| 104 | [MVP-104](tickets/MVP-104-create-k6-test-configuration.md) | System & Load Testing | [MVP-103](tickets/MVP-103-run-system-tests-on-staging.md) | Open |
| 105 | [MVP-105](tickets/MVP-105-add-k6-redirect-load-scenario.md) | System & Load Testing | [MVP-104](tickets/MVP-104-create-k6-test-configuration.md) | Open |
| 106 | [MVP-106](tickets/MVP-106-add-k6-creation-load-scenario.md) | System & Load Testing | [MVP-105](tickets/MVP-105-add-k6-redirect-load-scenario.md) | Open |
| 107 | [MVP-107](tickets/MVP-107-add-read-heavy-mixed-load-scenario.md) | System & Load Testing | [MVP-106](tickets/MVP-106-add-k6-creation-load-scenario.md) | Open |
| 108 | [MVP-108](tickets/MVP-108-set-mvp-load-thresholds.md) | System & Load Testing | [MVP-107](tickets/MVP-107-add-read-heavy-mixed-load-scenario.md) | Open |
| 109 | [MVP-109](tickets/MVP-109-run-k6-smoke-in-pr-ci.md) | System & Load Testing | [MVP-108](tickets/MVP-108-set-mvp-load-thresholds.md) | Open |
| 110 | [MVP-110](tickets/MVP-110-run-k6-gate-on-staging.md) | System & Load Testing | [MVP-109](tickets/MVP-109-run-k6-smoke-in-pr-ci.md) | Open |
| 111 | [MVP-111](tickets/MVP-111-add-main-pipeline-retest-gate.md) | System & Load Testing | [MVP-110](tickets/MVP-110-run-k6-gate-on-staging.md) | Open |
| 112 | [MVP-112](tickets/MVP-112-finalize-pipeline-promotion-gates.md) | System & Load Testing | [MVP-111](tickets/MVP-111-add-main-pipeline-retest-gate.md) | Open |
| 113 | [MVP-113](tickets/MVP-113-add-structured-application-logging.md) | Observability & Runbooks | [MVP-112](tickets/MVP-112-finalize-pipeline-promotion-gates.md) | Open |
| 114 | [MVP-114](tickets/MVP-114-add-request-correlation-ids.md) | Observability & Runbooks | [MVP-113](tickets/MVP-113-add-structured-application-logging.md) | Open |
| 115 | [MVP-115](tickets/MVP-115-add-safe-application-metrics.md) | Observability & Runbooks | [MVP-114](tickets/MVP-114-add-request-correlation-ids.md) | Open |
| 116 | [MVP-116](tickets/MVP-116-configure-railway-observability-views.md) | Observability & Runbooks | [MVP-115](tickets/MVP-115-add-safe-application-metrics.md) | Open |
| 117 | [MVP-117](tickets/MVP-117-configure-production-alerts.md) | Observability & Runbooks | [MVP-116](tickets/MVP-116-configure-railway-observability-views.md) | Open |
| 118 | [MVP-118](tickets/MVP-118-write-deployment-runbook.md) | Observability & Runbooks | [MVP-117](tickets/MVP-117-configure-production-alerts.md) | Open |
| 119 | [MVP-119](tickets/MVP-119-write-secret-rotation-runbook.md) | Observability & Runbooks | [MVP-118](tickets/MVP-118-write-deployment-runbook.md) | Open |
| 120 | [MVP-120](tickets/MVP-120-write-redis-recovery-runbook.md) | Observability & Runbooks | [MVP-119](tickets/MVP-119-write-secret-rotation-runbook.md) | Open |
| 121 | [MVP-121](tickets/MVP-121-rehearse-staging-application-rollback.md) | Observability & Runbooks | [MVP-120](tickets/MVP-120-write-redis-recovery-runbook.md) | Open |
| 122 | [MVP-122](tickets/MVP-122-rehearse-staging-secret-failure.md) | Observability & Runbooks | [MVP-121](tickets/MVP-121-rehearse-staging-application-rollback.md) | Open |
| 123 | [MVP-123](tickets/MVP-123-configure-production-domain-and-dns.md) | Production Readiness & Security | [MVP-122](tickets/MVP-122-rehearse-staging-secret-failure.md) | Open |
| 124 | [MVP-124](tickets/MVP-124-verify-production-tls.md) | Production Readiness & Security | [MVP-123](tickets/MVP-123-configure-production-domain-and-dns.md) | Open |
| 125 | [MVP-125](tickets/MVP-125-set-production-infisical-values.md) | Production Readiness & Security | [MVP-124](tickets/MVP-124-verify-production-tls.md) | Open |
| 126 | [MVP-126](tickets/MVP-126-add-concurrent-creation-integration-test.md) | Production Readiness & Security | [MVP-125](tickets/MVP-125-set-production-infisical-values.md) | Open |
| 127 | [MVP-127](tickets/MVP-127-add-generated-alias-uniqueness-integration-test.md) | Production Readiness & Security | [MVP-126](tickets/MVP-126-add-concurrent-creation-integration-test.md) | Open |
| 128 | [MVP-128](tickets/MVP-128-configure-dependency-vulnerability-scan.md) | Production Readiness & Security | [MVP-127](tickets/MVP-127-add-generated-alias-uniqueness-integration-test.md) | Open |
| 129 | [MVP-129](tickets/MVP-129-configure-container-vulnerability-scan.md) | Production Readiness & Security | [MVP-128](tickets/MVP-128-configure-dependency-vulnerability-scan.md) | Open |
| 130 | [MVP-130](tickets/MVP-130-generate-release-sbom.md) | Production Readiness & Security | [MVP-129](tickets/MVP-129-configure-container-vulnerability-scan.md) | Open |
| 131 | [MVP-131](tickets/MVP-131-configure-gitlab-deployment-credentials.md) | Production Readiness & Security | [MVP-130](tickets/MVP-130-generate-release-sbom.md) | Open |
| 132 | [MVP-132](tickets/MVP-132-configure-registry-retention-policy.md) | Production Readiness & Security | [MVP-131](tickets/MVP-131-configure-gitlab-deployment-credentials.md) | Open |
| 133 | [MVP-133](tickets/MVP-133-audit-environment-isolation.md) | Production Readiness & Security | [MVP-132](tickets/MVP-132-configure-registry-retention-policy.md) | Open |
| 134 | [MVP-134](tickets/MVP-134-run-staging-feature-persistence-rehearsal.md) | Production Readiness & Security | [MVP-133](tickets/MVP-133-audit-environment-isolation.md) | Open |
| 135 | [MVP-135](tickets/MVP-135-run-full-staging-acceptance-suite.md) | Production Readiness & Security | [MVP-134](tickets/MVP-134-run-staging-feature-persistence-rehearsal.md) | Open |
| 136 | [MVP-136](tickets/MVP-136-perform-production-release-preflight.md) | Production Readiness & Security | [MVP-135](tickets/MVP-135-run-full-staging-acceptance-suite.md) | Open |
| 137 | [MVP-137](tickets/MVP-137-promote-release-candidate-to-production.md) | Production Release & Closeout | [MVP-136](tickets/MVP-136-perform-production-release-preflight.md) | Open |
| 138 | [MVP-138](tickets/MVP-138-run-production-functional-smoke-tests.md) | Production Release & Closeout | [MVP-137](tickets/MVP-137-promote-release-candidate-to-production.md) | Open |
| 139 | [MVP-139](tickets/MVP-139-verify-production-mapping-survives-app-redeploy.md) | Production Release & Closeout | [MVP-138](tickets/MVP-138-run-production-functional-smoke-tests.md) | Open |
| 140 | [MVP-140](tickets/MVP-140-run-production-load-canary.md) | Production Release & Closeout | [MVP-139](tickets/MVP-139-verify-production-mapping-survives-app-redeploy.md) | Open |
| 141 | [MVP-141](tickets/MVP-141-verify-production-observability.md) | Production Release & Closeout | [MVP-140](tickets/MVP-140-run-production-load-canary.md) | Open |
| 142 | [MVP-142](tickets/MVP-142-finalize-operator-readme.md) | Production Release & Closeout | [MVP-141](tickets/MVP-141-verify-production-observability.md) | Open |
| 143 | [MVP-143](tickets/MVP-143-complete-requirements-traceability-signoff.md) | Production Release & Closeout | [MVP-142](tickets/MVP-142-finalize-operator-readme.md) | Open |
| 144 | [MVP-144](tickets/MVP-144-create-release-notes-and-tag.md) | Production Release & Closeout | [MVP-143](tickets/MVP-143-complete-requirements-traceability-signoff.md) | Open |
| 145 | [MVP-145](tickets/MVP-145-complete-post-deployment-watch.md) | Production Release & Closeout | [MVP-144](tickets/MVP-144-create-release-notes-and-tag.md) | Open |
