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

# Dedicated system user/group: no login shell, no home directory contents,
# least privilege for the running process.
RUN groupadd --system foldlink \
    && useradd --system --gid foldlink --no-create-home --shell /usr/sbin/nologin foldlink

WORKDIR /app
COPY --from=build --chown=foldlink:foldlink /workspace/build/libs/*.jar app.jar

USER foldlink

ENTRYPOINT ["java", "-jar", "app.jar"]
