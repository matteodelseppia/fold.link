#!/usr/bin/env bash
# CI-friendly container smoke test
# (docs/milestones/mvp/tickets/MVP-029-add-container-smoke-test-script.md).
#
# Starts a disposable Redis container and the given application image on an
# isolated Docker network, waits for the application to report ready,
# checks one HTTP endpoint, and always cleans up afterwards — on success or
# failure — without ever touching a developer's persistent Redis volume
# (docker-compose.yml's `foldlink-redis-data` is never referenced here).
#
# The readiness/HTTP checks run from a small curl container attached to the
# same isolated network (rather than publishing a host port and curling
# 127.0.0.1) so this works identically on a developer machine and inside a
# GitLab docker-in-docker CI job, where the daemon and its containers don't
# share the job's own network namespace.
#
# Usage:
#   scripts/container-smoke-test.sh <image-ref>
#
# Environment:
#   REDIS_IMAGE        Redis image to start (default: redis:7.4.2-alpine,
#                       same as docker-compose.yml / CI's unit_test service).
#   CURL_IMAGE          Image used to run the in-network HTTP checks
#                        (default: curlimages/curl:8.15.0).
#   EXTRA_DOCKER_ARGS   Extra `docker run` flags inserted before the image
#                       name, e.g. `--entrypoint=/bin/false` to deliberately
#                       make the container fail (used for testing this
#                       script itself).
#   LOG_DIR             If set, on failure the app/Redis container logs are
#                        additionally written to $LOG_DIR/app.log and
#                        $LOG_DIR/redis.log (in addition to stderr), so a
#                        caller (e.g. CI) can retain them as job artifacts.
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <image-ref>" >&2
  exit 1
fi
IMAGE="$1"
REDIS_IMAGE="${REDIS_IMAGE:-redis:7.4.2-alpine}"
CURL_IMAGE="${CURL_IMAGE:-curlimages/curl:8.15.0}"
EXTRA_ARGS=()
if [ -n "${EXTRA_DOCKER_ARGS:-}" ]; then
  read -ra EXTRA_ARGS <<<"$EXTRA_DOCKER_ARGS"
fi

# Collision-safe: unique per invocation so concurrent CI jobs (or a CI run
# alongside a developer's local `docker compose up`) never clash on
# container or network names.
RUN_ID="smoke-$$-$(date +%s)"
NETWORK="foldlink-${RUN_ID}-net"
REDIS_NAME="foldlink-${RUN_ID}-redis"
APP_NAME="foldlink-${RUN_ID}-app"
APP_PORT=8080

READY_RETRIES=30
READY_INTERVAL=2

cleanup() {
  local exit_code=$?
  if [ "$exit_code" -ne 0 ]; then
    echo "--- smoke test FAILED (run ${RUN_ID}); diagnostic logs follow ---" >&2
    echo "--- ${APP_NAME} logs ---" >&2
    docker logs "$APP_NAME" >&2 2>&1 || true
    echo "--- ${REDIS_NAME} logs ---" >&2
    docker logs "$REDIS_NAME" >&2 2>&1 || true
    if [ -n "${LOG_DIR:-}" ]; then
      mkdir -p "$LOG_DIR"
      docker logs "$APP_NAME" >"$LOG_DIR/app.log" 2>&1 || true
      docker logs "$REDIS_NAME" >"$LOG_DIR/redis.log" 2>&1 || true
    fi
  fi
  echo "Cleaning up smoke-test resources (run ${RUN_ID})..."
  # Only ever removes the ephemeral, uniquely-named resources created by
  # this run above — never a developer's persistent Redis volume.
  docker rm -f "$APP_NAME" "$REDIS_NAME" >/dev/null 2>&1 || true
  docker network rm "$NETWORK" >/dev/null 2>&1 || true
  exit "$exit_code"
}
trap cleanup EXIT

curl_in_network() {
  docker run --rm --network "$NETWORK" "$CURL_IMAGE" "$@"
}

docker network create "$NETWORK" >/dev/null
docker run -d --name "$REDIS_NAME" --network "$NETWORK" "$REDIS_IMAGE" >/dev/null

docker run -d --name "$APP_NAME" --network "$NETWORK" \
  -e REDIS_HOST="$REDIS_NAME" \
  -e REDIS_PORT=6379 \
  -e APP_BASE_URL="http://localhost:${APP_PORT}" \
  "${EXTRA_ARGS[@]+"${EXTRA_ARGS[@]}"}" \
  "$IMAGE" >/dev/null

echo "Waiting for ${APP_NAME} to become ready (up to $((READY_RETRIES * READY_INTERVAL))s)..."
ready=false
for attempt in $(seq 1 "$READY_RETRIES"); do
  status="$(docker inspect --format='{{.State.Status}}' "$APP_NAME" 2>/dev/null || echo "missing")"
  if [ "$status" != "running" ]; then
    echo "ERROR: ${APP_NAME} is not running (status: ${status}) on attempt ${attempt}." >&2
    exit 1
  fi
  if curl_in_network -sf -o /dev/null "http://${APP_NAME}:${APP_PORT}/actuator/health/readiness"; then
    ready=true
    break
  fi
  sleep "$READY_INTERVAL"
done

if [ "$ready" != true ]; then
  echo "ERROR: ${APP_NAME} did not become ready within $((READY_RETRIES * READY_INTERVAL))s." >&2
  exit 1
fi

echo "Checking GET /actuator/health/readiness..."
response="$(curl_in_network -sf "http://${APP_NAME}:${APP_PORT}/actuator/health/readiness")"
echo "$response"
if ! echo "$response" | grep -q '"status":"UP"'; then
  echo "ERROR: readiness endpoint did not report status UP." >&2
  exit 1
fi

echo "Container smoke test passed for image: ${IMAGE}"
