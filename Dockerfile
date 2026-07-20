# syntax=docker/dockerfile:1
#
# Multi-stage build for the fold.link Spring Boot 4 / Java 25 backend.
# See docs/milestones/mvp/tickets/MVP-026-add-multi-stage-dockerfile.md.
#
# Both stages are pinned by digest (not just tag) so a rebuild always
# resolves to the exact same upstream image, even if the tag is later
# retagged upstream.

# ---- Build stage: resolves dependencies and produces the executable jar ----
FROM eclipse-temurin:25.0.3_9-jdk-noble@sha256:3eb81ed94d8c1a34422f19f8188548bdf02cae69c91d0328afdbb7abed90f617 AS build
WORKDIR /workspace

# Wrapper, lockfiles and build scripts first so dependency resolution is
# cached across builds that only change application source.
COPY gradlew build.gradle settings.gradle gradle.lockfile settings-gradle.lockfile ./
COPY gradle gradle
RUN ./gradlew --version --console=plain --no-daemon

COPY src src
RUN ./gradlew bootJar --console=plain --no-daemon

# ---- Runtime stage: only the JRE and the built jar, run as non-root ----
FROM eclipse-temurin:25.0.3_9-jre-noble@sha256:2f1da100788559b397bcf48c736169ea5b070bde84e55f203bbee8e83d87a175 AS runtime

# Matches server.port's default in application.yml (SERVER_PORT env var).
ENV SERVER_PORT=8080
EXPOSE ${SERVER_PORT}

# Pinned Infisical CLI (matches docs/milestones/mvp/toolchain.md's v0.41.89
# pin). Downloaded directly from the GitHub release (not the packagecloud
# apt repo) so the exact binary can be checksum-verified against the
# release's published checksums.txt, consistent with how the base images
# above are pinned by digest rather than by a mutable tag. curl/ca-certs
# are installed only for this step and purged afterward, along with the apt
# metadata, so the runtime image doesn't retain build-time-only tooling. No
# Infisical credential is ever present here -- only the CLI binary itself.
RUN set -eux; \
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates curl; \
    infisical_version="0.41.89"; \
    arch="$(dpkg --print-architecture)"; \
    case "$arch" in \
      amd64) checksum="6dd031a62d12f0d04209a2ff296978bbf8178214f059b46aad5d433c02b12854" ;; \
      arm64) checksum="4a16c82b04e30b816f879b1cab74c39bb614a025a03190a76d15f8d2c74609d5" ;; \
      *) echo "unsupported architecture for Infisical CLI: ${arch}" >&2; exit 1 ;; \
    esac; \
    tarball="infisical_${infisical_version}_linux_${arch}.tar.gz"; \
    curl -fsSLO "https://github.com/Infisical/infisical/releases/download/infisical-cli/v${infisical_version}/${tarball}"; \
    echo "${checksum}  ${tarball}" | sha256sum -c -; \
    tar -xzf "${tarball}" infisical; \
    install -o root -g root -m 0755 infisical /usr/local/bin/infisical; \
    rm -f "${tarball}" infisical; \
    apt-get purge -y --auto-remove curl; \
    rm -rf /var/lib/apt/lists/*

# Dedicated system user/group: no login shell, no home directory contents,
# least privilege for the running process.
RUN groupadd --system foldlink \
    && useradd --system --gid foldlink --no-create-home --shell /usr/sbin/nologin foldlink

WORKDIR /app
COPY --from=build --chown=foldlink:foldlink /workspace/build/libs/*.jar app.jar
COPY --chmod=0755 docker/entrypoint.sh /usr/local/bin/docker-entrypoint.sh

USER foldlink

# Liveness only (not readiness): a Redis outage must never make the
# container orchestrator kill and restart an otherwise-healthy JVM
# process (see docs/milestones/mvp/health-endpoints.md). curl/wget are
# not present in this minimal runtime image, so the probe speaks raw
# HTTP/1.1 over bash's built-in /dev/tcp and checks the JSON body for
# "status":"UP" against the runtime port (SERVER_PORT, not a
# hard-coded development port).
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=3 \
    CMD bash -c '\
        exec 3<>/dev/tcp/127.0.0.1/${SERVER_PORT} \
        && printf "GET /actuator/health/liveness HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n" >&3 \
        && grep -q "\"status\":\"UP\"" <&3 \
    ' || exit 1

ENTRYPOINT ["docker-entrypoint.sh"]
