#!/usr/bin/env bash
# Integration tests for docker/entrypoint.sh
# (docs/milestones/mvp/tickets/MVP-037-add-infisical-runtime-entrypoint.md).
#
# Builds a throwaway image that layers fake `infisical`/`java` binaries
# (docker/test-fixtures/entrypoint-test/) on top of the given application
# image, then runs the real entrypoint against those fakes -- reordering
# PATH so they're found instead of the real binaries -- to exercise:
#   1. no bootstrap vars set -> Infisical skipped, java starts directly
#   2. a partial set of bootstrap vars -> refuses to start
#   3. login failure -> refuses to start, client secret never appears in
#      output (even though the fake CLI deliberately echoes it back)
#   4. export failure -> refuses to start
#   5. full success -> the fetched secret is visible in java's environment
#   6. SIGTERM sent to the container reaches java directly and it exits
#      promptly (no wrapper process left in front of it)
#
# This never talks to a real Infisical account and needs no credentials.
#
# Usage:
#   scripts/test-infisical-entrypoint.sh <image-ref>
set -euo pipefail

if [ "$#" -ne 1 ]; then
  echo "usage: $0 <image-ref>" >&2
  exit 1
fi
APP_IMAGE="$1"
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
FIXTURES_DIR="$REPO_ROOT/docker/test-fixtures/entrypoint-test"

RUN_ID="entrypoint-test-$$-$(date +%s)"
TEST_IMAGE="foldlink-app-test-doubles:${RUN_ID}"
FAKE_PATH="/opt/test-doubles:/opt/java/openjdk/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"

failures=0
containers_to_clean=()

cleanup() {
  local exit_code=$?
  for c in "${containers_to_clean[@]+"${containers_to_clean[@]}"}"; do
    docker rm -f "$c" >/dev/null 2>&1 || true
  done
  docker rmi "$TEST_IMAGE" >/dev/null 2>&1 || true
  exit "$exit_code"
}
trap cleanup EXIT

fail() {
  echo "FAIL: $1" >&2
  failures=$((failures + 1))
}

pass() {
  echo "PASS: $1"
}

docker build -q -f "$FIXTURES_DIR/Dockerfile" --build-arg BASE_IMAGE="$APP_IMAGE" \
  -t "$TEST_IMAGE" "$FIXTURES_DIR" >/dev/null

# --- Test 1: no bootstrap vars -> Infisical skipped, java starts directly ---
name="foldlink-${RUN_ID}-t1"
containers_to_clean+=("$name")
docker run -d --name "$name" \
  -e PATH="$FAKE_PATH" \
  -e REDIS_HOST=passthrough-value \
  "$TEST_IMAGE" >/dev/null
sleep 2
logs="$(docker logs "$name" 2>&1)"
docker stop -t 2 "$name" >/dev/null 2>&1 || true
if echo "$logs" | grep -q "starting without Infisical" && echo "$logs" | grep -q "FAKE_JAVA_STARTED.*REDIS_HOST=passthrough-value"; then
  pass "no bootstrap vars: Infisical skipped, java started with plain env passed through"
else
  fail "no bootstrap vars: expected skip + java startup with REDIS_HOST passthrough. Got:
$logs"
fi

# --- Test 2: partial bootstrap vars -> refuses to start ---
name="foldlink-${RUN_ID}-t2"
containers_to_clean+=("$name")
set +e
logs="$(docker run --rm --name "$name" -e PATH="$FAKE_PATH" \
  -e INFISICAL_CLIENT_ID=only-this-one \
  "$TEST_IMAGE" 2>&1)"
status=$?
set -e
if [ "$status" -ne 0 ] && echo "$logs" | grep -q "missing:" && ! echo "$logs" | grep -q "FAKE_JAVA_STARTED"; then
  pass "partial bootstrap vars: refused to start, java never reached"
else
  fail "partial bootstrap vars: expected non-zero exit and no java startup. status=$status. Got:
$logs"
fi

# --- Test 3: login failure -> refuses to start, secret never printed ---
name="foldlink-${RUN_ID}-t3"
containers_to_clean+=("$name")
set +e
logs="$(docker run --rm --name "$name" -e PATH="$FAKE_PATH" \
  -e INFISICAL_CLIENT_ID=id -e INFISICAL_CLIENT_SECRET=TOP-SECRET-VALUE \
  -e INFISICAL_PROJECT_ID=proj -e INFISICAL_ENV_SLUG=staging \
  -e FAKE_INFISICAL_LOGIN_FAIL=1 \
  "$TEST_IMAGE" 2>&1)"
status=$?
set -e
if [ "$status" -ne 0 ] && echo "$logs" | grep -qi "authentication failed" \
  && ! echo "$logs" | grep -q "TOP-SECRET-VALUE" && ! echo "$logs" | grep -q "FAKE_JAVA_STARTED"; then
  pass "login failure: refused to start, client secret redacted, java never reached"
else
  fail "login failure: expected redacted auth error and no java startup. status=$status. Got:
$logs"
fi

# --- Test 4: export failure -> refuses to start ---
name="foldlink-${RUN_ID}-t4"
containers_to_clean+=("$name")
set +e
logs="$(docker run --rm --name "$name" -e PATH="$FAKE_PATH" \
  -e INFISICAL_CLIENT_ID=id -e INFISICAL_CLIENT_SECRET=secret \
  -e INFISICAL_PROJECT_ID=proj -e INFISICAL_ENV_SLUG=staging \
  -e FAKE_INFISICAL_EXPORT_FAIL=1 \
  "$TEST_IMAGE" 2>&1)"
status=$?
set -e
if [ "$status" -ne 0 ] && echo "$logs" | grep -qi "failed to fetch secrets" && ! echo "$logs" | grep -q "FAKE_JAVA_STARTED"; then
  pass "export failure: refused to start, java never reached"
else
  fail "export failure: expected clear error and no java startup. status=$status. Got:
$logs"
fi

# --- Test 5 & 6: full success -> secret injected; SIGTERM reaches java ---
name="foldlink-${RUN_ID}-t56"
containers_to_clean+=("$name")
docker run -d --name "$name" -e PATH="$FAKE_PATH" \
  -e INFISICAL_CLIENT_ID=id -e INFISICAL_CLIENT_SECRET=secret \
  -e INFISICAL_PROJECT_ID=proj -e INFISICAL_ENV_SLUG=staging \
  "$TEST_IMAGE" >/dev/null
sleep 2
logs="$(docker logs "$name" 2>&1)"
if echo "$logs" | grep -q "FAKE_JAVA_STARTED.*FAKE_SECRET=from-infisical"; then
  pass "full success: fetched secret visible in java's environment"
else
  fail "full success: expected FAKE_SECRET=from-infisical in java's environment. Got:
$logs"
fi

pid1_cmd="$(docker exec "$name" cat /proc/1/cmdline 2>/dev/null | tr '\0' ' ')"
if echo "$pid1_cmd" | grep -q "test-doubles/java"; then
  pass "full success: java (the fake) is the container's PID 1, not a wrapper"
else
  fail "full success: expected the fake java to be PID 1. Got: ${pid1_cmd}"
fi

start_ts=$(date +%s)
docker stop -t 10 "$name" >/dev/null
end_ts=$(date +%s)
elapsed=$((end_ts - start_ts))
logs="$(docker logs "$name" 2>&1)"
if echo "$logs" | grep -q "FAKE_JAVA_GOT_SIGTERM" && [ "$elapsed" -lt 5 ]; then
  pass "SIGTERM propagation: java received SIGTERM directly and exited promptly (${elapsed}s)"
else
  fail "SIGTERM propagation: expected FAKE_JAVA_GOT_SIGTERM and a fast stop (<5s), took ${elapsed}s. Got:
$logs"
fi

echo "---"
if [ "$failures" -eq 0 ]; then
  echo "All docker/entrypoint.sh integration tests passed."
  exit 0
else
  echo "${failures} docker/entrypoint.sh integration test(s) FAILED." >&2
  exit 1
fi
