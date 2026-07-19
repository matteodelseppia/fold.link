#!/usr/bin/env bash
# Run the application locally with configuration injected by the Infisical
# CLI (see docs/milestones/mvp/tickets/MVP-031-create-infisical-project-and-environments.md
# for the project/environment setup this depends on). Never prints secret
# values — infisical injects them directly into the child process
# environment, they are not echoed by this script or by `infisical run`.
set -euo pipefail
cd "$(dirname "$0")/../.."

if ! command -v infisical >/dev/null 2>&1; then
  echo "ERROR: the infisical CLI is not installed or not on PATH." >&2
  echo "Install it (see docs/milestones/mvp/toolchain.md), or for a quick" >&2
  echo "local run without Infisical: copy .env.example to .env, then run" >&2
  echo "  set -a && source .env && set +a && ./gradlew bootRun" >&2
  exit 1
fi

exec infisical run --env=dev -- ./gradlew bootRun
