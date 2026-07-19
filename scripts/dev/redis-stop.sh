#!/usr/bin/env bash
# Stop the local Compose Redis service. Preserves the named
# foldlink-redis-data volume by default, so restarting keeps existing data.
# Pass --purge-volume to also delete the volume (all local Redis data lost).
set -euo pipefail
cd "$(dirname "$0")/../.."

if [ "${1:-}" = "--purge-volume" ]; then
  docker compose down -v
  echo "Redis stopped and the foldlink-redis-data volume was removed."
else
  docker compose down
  echo "Redis stopped. The foldlink-redis-data volume was preserved (re-run with --purge-volume to delete it)."
fi
