#!/usr/bin/env bash
# One-off migration: re-expire every existing link mapping (and its click
# counter) to the new 30-day default TTL (app.redis.ttl / APP_REDIS_TTL,
# see AppProperties/application.yml), so links created under the old 3-day
# default aren't left to expire early.
#
# Uses `redis-cli --scan` (cursor-based, non-blocking) to walk all keys
# under the configured key prefix (v1:link:* by default, covering both
# v1:link:{alias} mappings and v1:link:{alias}:clicks counters) and issues
# EXPIRE <key> <ttl-seconds> for each one that already exists (a TTL race
# with a key expiring mid-scan is harmless: EXPIRE on a missing key is a
# no-op).
#
# Usage:
#   scripts/migrate-ttl-to-30-days.sh [key-prefix] [ttl-seconds]
#
# Environment (same as the application's Redis connection):
#   REDIS_HOST      Redis host (required)
#   REDIS_PORT      Redis port (default: 6379)
#   REDIS_PASSWORD  Redis password (default: none)
set -euo pipefail

KEY_PREFIX="${1:-v1:link:}"
TTL_SECONDS="${2:-2592000}" # 30 days

if [ -z "${REDIS_HOST:-}" ]; then
  echo "REDIS_HOST must be set" >&2
  exit 1
fi

REDIS_CLI=(redis-cli -h "$REDIS_HOST" -p "${REDIS_PORT:-6379}")
if [ -n "${REDIS_PASSWORD:-}" ]; then
  REDIS_CLI+=(-a "$REDIS_PASSWORD" --no-auth-warning)
fi

count=0
while IFS= read -r key; do
  [ -z "$key" ] && continue
  "${REDIS_CLI[@]}" expire "$key" "$TTL_SECONDS" >/dev/null
  count=$((count + 1))
done < <("${REDIS_CLI[@]}" --scan --pattern "${KEY_PREFIX}*")

echo "Re-expired ${count} key(s) matching '${KEY_PREFIX}*' to ${TTL_SECONDS}s."
