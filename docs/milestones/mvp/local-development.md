# Local development commands

Small, documented commands for local development. None of them print secret
values.

| Command | Purpose |
| --- | --- |
| `scripts/dev/redis-start.sh` | Start the Compose Redis service and wait for it to report healthy (fails clearly after 60s if it doesn't). |
| `scripts/dev/run.sh` | Run the application with configuration injected by the Infisical CLI (falls back to instructions for `.env` if Infisical isn't installed/configured yet). |
| `scripts/dev/test.sh` | Run the test suite via the pinned Gradle wrapper. |
| `scripts/dev/redis-stop.sh` | Stop Redis. Preserves the `foldlink-redis-data` volume by default; pass `--purge-volume` to also delete it. |

Typical flow from a fresh shell:

```shell
scripts/dev/redis-start.sh
scripts/dev/test.sh
scripts/dev/run.sh      # or: cp .env.example .env, edit it, then
                         # set -a && source .env && set +a && ./gradlew bootRun
scripts/dev/redis-stop.sh
```
