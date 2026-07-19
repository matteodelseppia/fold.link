# Health endpoints

Only the Actuator `health` endpoint is ever web-exposed (`management.endpoints.web.exposure.include: health` — no `env`, `beans`, `metrics`, etc., in any profile).

| Path | Purpose | Depends on Redis |
| --- | --- | --- |
| `/actuator/health` | Aggregate status (component detail level controlled by `ACTUATOR_HEALTH_SHOW_DETAILS`, `never` by default and in production) | Yes (aggregates all indicators) |
| `/actuator/health/liveness` | Container/Railway **liveness** probe: is the JVM process itself still running? | No — a Redis outage must never make an orchestrator kill and restart an otherwise-healthy instance |
| `/actuator/health/readiness` | Container/Railway **readiness** probe: can this instance actually serve traffic? | Yes — the app cannot create/redirect links without Redis, so readiness must fail (and traffic should stop routing here) while Redis is unreachable |

Group membership is configured explicitly in `src/main/resources/application.yml` (`management.endpoint.health.group.liveness` / `.readiness`) rather than relying on Spring Boot's defaults, so this behavior can't silently drift.
