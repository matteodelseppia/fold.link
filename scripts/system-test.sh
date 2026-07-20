#!/usr/bin/env bash
# CI system test.
#
# Brings up the full docker-compose.system-test.yml stack (Redis + the
# given application image) in a disposable, uniquely-named Compose
# project, waits for both services to report healthy, then runs the Node
# test runner suite in test/system/ against the live HTTP API - currently
# the health endpoints and the link-creation/redirect APIs
# (test/system/links.test.mjs).
#
# Unlike scripts/container-smoke-test.sh (which curls from a container on
# an isolated Docker network to also work under GitLab docker-in-docker),
# this publishes the app's port to the host and hits it directly from
# Node -- fine here because this runs on a GitHub Actions runner, which
# shares its network namespace with containers it starts.
#
# Usage:
#   scripts/system-test.sh <image-ref>
#
# Environment:
#   REDIS_IMAGE   Redis image to start (default: redis:7.4.2-alpine, same
#                 as docker-compose.yml / CI's unit_test service).
#   APP_PORT      Host/container port the app listens on and is published
#                 on (default: 8080).
#   LOG_DIR       If set, on failure the compose project's logs are also
#                 written to $LOG_DIR/compose.log (in addition to stderr),
#                 so a caller (e.g. CI) can retain them as job artifacts.
set -euo pipefail
cd "$(dirname "$0")/.."

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <image-ref>" >&2
  exit 1
fi

export APP_IMAGE="$1"
export REDIS_IMAGE="${REDIS_IMAGE:-redis:7.4.2-alpine}"
export APP_PORT="${APP_PORT:-8080}"

# Collision-safe: unique per invocation so concurrent CI jobs never clash
# on Compose project, container, or network names.
RUN_ID="system-test-$$-$(date +%s)"
COMPOSE=(docker compose -p "$RUN_ID" -f docker-compose.system-test.yml)

cleanup() {
  local exit_code=$?
  if [ "$exit_code" -ne 0 ]; then
    echo "--- system test FAILED (run ${RUN_ID}); diagnostic logs follow ---" >&2
    "${COMPOSE[@]}" logs >&2 2>&1 || true
    if [ -n "${LOG_DIR:-}" ]; then
      mkdir -p "$LOG_DIR"
      "${COMPOSE[@]}" logs >"$LOG_DIR/compose.log" 2>&1 || true
    fi
  fi
  echo "Cleaning up system-test resources (run ${RUN_ID})..."
  "${COMPOSE[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
  exit "$exit_code"
}
trap cleanup EXIT

echo "Starting system-test stack (run ${RUN_ID})..."
"${COMPOSE[@]}" up -d --wait --wait-timeout 90

echo "Running system test suite against http://localhost:${APP_PORT}..."
APP_BASE_URL="http://localhost:${APP_PORT}" node --test test/system/*.test.mjs

echo "System tests passed for image: ${APP_IMAGE}"
