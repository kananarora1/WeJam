# Decisions

ADR-style log: context → decision → why.

## ADR-001: Maven for the backend build
- **Context:** CLAUDE.md §7/§8 originally specified Gradle; the backend was generated with Maven. CLAUDE.md updated to Maven.
- **Decision:** Use Maven (`./mvnw`).
- **Why:** Developer preference; existing project already on Maven.

## ADR-002: Docker Compose for local Postgres/Redis
- **Context:** Need Postgres + PostGIS and Redis locally; tests already require Docker (Testcontainers).
- **Decision:** Run both via `infra/docker-compose.yml`, same images as tests. Production will use a managed DB/Redis, configured purely via env vars.
- **Why:** PostGIS bundled with a matching Postgres version, dev/test parity, one-command reset (`down -v`), no host installs.

## ADR-003: Host port 5433 for Postgres
- **Context:** Port 5432 is already used on the dev machine by another project's container.
- **Decision:** Map container 5432 → host 5433; app default `DB_URL` uses 5433. Override via `infra/.env` / `DB_URL`.
- **Why:** Avoids conflicts without stopping unrelated services.

## ADR-004: Schema owned by Flyway; `open-in-view` disabled
- **Context:** CLAUDE.md forbids `ddl-auto=update`; §5.8 requires avoiding N+1.
- **Decision:** `ddl-auto=validate`; `spring.jpa.open-in-view=false`.
- **Why:** Flyway is the single schema writer (same migrations run in prod); OSIV off makes lazy-loading outside services fail loudly instead of issuing hidden queries.
