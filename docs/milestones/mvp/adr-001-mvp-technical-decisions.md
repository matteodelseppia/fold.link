# ADR-001: MVP Technical Decisions

**Status**: Accepted

## Contracts

| Decision | Value |
| --- | --- |
| Create endpoint | `POST /api/v1/links` |
| Redirect endpoint | `GET /{alias}` (unversioned — it is the public short URL) |
| Redirect status | `302 Found` (prevents browser/CDN caching) |
| Accepted URL schemes | `http`, `https` only |
| Alias format | 8 Base64url characters (`A–Z a–z 0–9 - _`), `SecureRandom` source |
| Redis key | `v1:link:{alias}` → destination URL as plain UTF-8 string |
| Mapping TTL | 3 days by default (`app.redis.ttl` / `APP_REDIS_TTL`), applied on create |
| Build tool | Gradle Wrapper (`./gradlew`) — see [design.md §1.1](design.md) |
| Frontend serving | Spring Boot static resources (`src/main/resources/static/`) — see [design.md §1.1](design.md) |

## Request / Response

**`POST /api/v1/links`** — body: `{ "url": "<destination>" }`

| Outcome | Status | Body fields |
| --- | --- | --- |
| Created | 201 | `alias`, `shortUrl`, `destination` |
| Invalid URL | 400 | `error: "VALIDATION_ERROR"`, `message` |
| Alias not found (`GET`) | 404 | `error: "ALIAS_NOT_FOUND"`, `message` |
| Storage unavailable | 503 | `error: "STORAGE_ERROR"`, `message` |

Requirements traced: F01, F02, F03, F05, NF01, NF04 — see [requirements.md](requirements.md).
