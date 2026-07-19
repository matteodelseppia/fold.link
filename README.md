# fold.link

fold.link is a URL shortener: it accepts a long URL, generates a short unique
alias for it, and redirects visitors from that alias back to the original
URL.

## MVP Scope

The MVP delivers a minimal but complete shortening flow:

- Accept a valid long URL from a user and generate a unique short alias.
- Redirect visitors to the original URL when a valid short URL is accessed.
- Return `404 Not Found` when a short URL alias does not exist.
- Provide a web UI to submit long URLs and copy the resulting short URL.
- Reject malformed URLs with a clear validation error.
- Persist URL mappings so short URLs keep working across restarts and
  deployments.
- Securely manage all environment configuration and secrets.
- Be designed and tested for a read-heavy workload (redirects far outnumber
  creations).
- Generate aliases that are unique and unguessable enough to make accidental
  collisions practically negligible.

See [docs/milestones/mvp/requirements.md](docs/milestones/mvp/requirements.md)
for the full functional and non-functional requirements.

## Architecture Summary

- **Spring Boot 4 backend** (Java 25) — exposes the REST API for URL
  creation (`POST /api/v1/links`) and handles the HTTP redirect logic
  (`GET /{alias}`).
- **Static frontend** (HTML/CSS/JS) — served directly by the Spring Boot
  backend from `src/main/resources/static/`, keeping the MVP simple and
  cohesive.
- **Redis storage** — the primary data store for URL mappings, chosen for
  in-memory read speed (critical for fast redirects) with persistence
  (RDB/AOF) so mappings survive restarts.
- **Infisical secrets** — sensitive configuration (e.g. Redis credentials)
  is stored in Infisical and injected into the runtime environment rather
  than committed to the repository.
- **Railway deploy** — the application is deployed to Railway, which fetches
  secrets from Infisical at runtime and hosts the staging and production
  environments.

Full architecture details, diagrams, and the release/testing pipeline are in
[docs/milestones/mvp/design.md](docs/milestones/mvp/design.md). Key technical
decisions (endpoint contracts, alias format, Redis key scheme, etc.) are
recorded in
[docs/milestones/mvp/adr-001-mvp-technical-decisions.md](docs/milestones/mvp/adr-001-mvp-technical-decisions.md).

## Prerequisites

To build, run, and test this project locally you will need:

- **Java 25**
- **Docker**
- **Node.js** (for the system test suite)
- **k6** (for load testing)
- **Infisical CLI** (for fetching local development secrets)
- **Redis** (as the local persistence backend)

## Planning documents

- [Requirements](docs/milestones/mvp/requirements.md)
- [Design](docs/milestones/mvp/design.md)
- [ADR-001: MVP Technical Decisions](docs/milestones/mvp/adr-001-mvp-technical-decisions.md)
- [Ticket index](docs/milestones/mvp/tickets/INDEX.md)
