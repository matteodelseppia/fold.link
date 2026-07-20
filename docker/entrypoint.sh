#!/usr/bin/env bash
# Infisical runtime entrypoint
# (docs/milestones/mvp/tickets/MVP-037-add-infisical-runtime-entrypoint.md).
#
# If this environment's Infisical bootstrap variables (below) are present,
# authenticates as this environment's machine identity, fetches its
# secrets, and exports them into this shell's environment before handing
# off to Java. If none of them are present, Infisical is skipped entirely
# and Java starts directly against whatever plain environment variables
# were already provided (this is what local `docker run` and the CI
# container smoke test do -- there is nothing secret to fetch there; see
# docs/milestones/mvp/tickets/MVP-033-populate-development-secrets.md).
# A *partial* set of bootstrap variables is always a misconfiguration and
# fails loudly rather than silently guessing.
#
# In both paths, this script ends by `exec`ing java, so java replaces this
# script as the container's PID 1 and receives termination signals
# directly -- no wrapper process is ever left in front of it.
#
# Bootstrap environment variables (provided by Railway for staging/
# production, or protected GitLab CI variables for deployment jobs; never
# logged, never persisted -- see MVP-038 for where they themselves are
# allowed to live):
#   INFISICAL_CLIENT_ID       Universal Auth client ID for this
#                              environment's machine identity.
#   INFISICAL_CLIENT_SECRET   Universal Auth client secret for the same
#                              identity.
#   INFISICAL_PROJECT_ID      The fold.link Infisical project ID.
#   INFISICAL_ENV_SLUG        Environment slug to fetch secrets from
#                              (e.g. staging, production).
set -euo pipefail

BOOTSTRAP_VARS=(INFISICAL_CLIENT_ID INFISICAL_CLIENT_SECRET INFISICAL_PROJECT_ID INFISICAL_ENV_SLUG)

set_count=0
for var in "${BOOTSTRAP_VARS[@]}"; do
  if [ -n "${!var:-}" ]; then
    set_count=$((set_count + 1))
  fi
done

if [ "$set_count" -eq 0 ]; then
  echo "INFISICAL_* bootstrap variables not set; starting without Infisical." >&2
  exec java -jar /app/app.jar "$@"
fi

if [ "$set_count" -lt "${#BOOTSTRAP_VARS[@]}" ]; then
  echo "ERROR: some but not all Infisical bootstrap variables are set. This is a misconfiguration -- refusing to start." >&2
  for var in "${BOOTSTRAP_VARS[@]}"; do
    if [ -z "${!var:-}" ]; then
      echo "  missing: ${var}" >&2
    fi
  done
  exit 1
fi

login_err="$(mktemp)"
# --plain -> stdout is exactly the access token, nothing else. The token
# itself is never echoed or logged by this script.
if ! INFISICAL_TOKEN="$(infisical login \
    --method=universal-auth \
    --client-id="$INFISICAL_CLIENT_ID" \
    --client-secret="$INFISICAL_CLIENT_SECRET" \
    --plain --silent 2>"$login_err")"; then
  echo "ERROR: Infisical authentication failed for this machine identity. Redacted detail:" >&2
  # Defense in depth: the CLI itself should never print the client secret,
  # but never trust that blindly -- scrub it from any error output anyway.
  sed "s/${INFISICAL_CLIENT_SECRET}/[REDACTED]/g" "$login_err" >&2 || true
  rm -f "$login_err"
  exit 1
fi
rm -f "$login_err"

export_out="$(mktemp)"
export_err="$(mktemp)"
# Captured as a plain command (not `source <(...)`): a process substitution's
# exit status is not visible to `source`, which would happily "succeed" by
# sourcing whatever partial/empty output was produced even if the command
# feeding it failed. Writing to a file first and checking infisical export's
# own exit code avoids silently continuing past a failed fetch.
if ! infisical export \
    --token="$INFISICAL_TOKEN" \
    --projectId="$INFISICAL_PROJECT_ID" \
    --env="$INFISICAL_ENV_SLUG" \
    --format=dotenv --silent >"$export_out" 2>"$export_err"; then
  echo "ERROR: failed to fetch secrets from Infisical (project/environment/path misconfigured, or this identity lacks read access). Redacted detail:" >&2
  sed "s/${INFISICAL_TOKEN}/[REDACTED]/g" "$export_err" >&2 || true
  rm -f "$export_out" "$export_err"
  exit 1
fi
rm -f "$export_err"

set -a
# shellcheck disable=SC1090
source "$export_out"
set +a
rm -f "$export_out"

# The bootstrap credentials and the short-lived access token are no longer
# needed once secrets are exported; don't hand them to the Java process.
unset INFISICAL_CLIENT_SECRET INFISICAL_TOKEN

exec java -jar /app/app.jar "$@"
