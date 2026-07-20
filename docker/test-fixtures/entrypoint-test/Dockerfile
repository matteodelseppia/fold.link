# syntax=docker/dockerfile:1
#
# Test-only image: layers fake `infisical`/`java` binaries on top of the
# real application image (built from the repository's own Dockerfile) so
# docker/entrypoint.sh's logic -- auth, secret export, signal handoff -- can
# be exercised deterministically without a real Infisical account or a real
# JVM. Never published; built and discarded by
# scripts/test-infisical-entrypoint.sh.
ARG BASE_IMAGE
FROM ${BASE_IMAGE}
COPY --chmod=0755 fake-infisical /opt/test-doubles/infisical
COPY --chmod=0755 fake-java /opt/test-doubles/java
