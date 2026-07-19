#!/usr/bin/env bash
# Start the local Compose Redis service and wait for it to report healthy.
# Fails clearly (non-zero exit, message on stderr) instead of hanging or
# silently returning before Redis is actually ready.
set -euo pipefail
cd "$(dirname "$0")/../.."

docker compose up -d redis

echo "Waiting for Redis to report healthy..."
for _ in $(seq 1 30); do
  health="$(docker inspect --format='{{.State.Health.Status}}' foldlink-redis-local 2>/dev/null || echo "starting")"
  if [ "$health" = "healthy" ]; then
    echo "Redis is healthy (127.0.0.1:6379)."
    exit 0
  fi
  sleep 2
done

echo "ERROR: Redis did not report healthy within 60s. Run 'docker compose logs redis' to investigate." >&2
exit 1
