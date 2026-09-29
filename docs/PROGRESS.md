# Progress

Log of completed steps (what was built, key decisions). Newest at the bottom.

## Step 1 — Monorepo skeleton, local infra, backend boot-up (2026-09-29)
- Monorepo layout: `backend/` (Maven, Spring Boot 4.1.1), `mobile/` (placeholder), `infra/`, `docs/`; combined root `.gitignore`.
- `infra/docker-compose.yml`: PostGIS 17-3.5 (host port 5433) + Redis 7, healthchecks, named volume.
- Backend deps added: actuator, data-jpa, data-redis, flyway (+ postgresql module), postgresql driver; test: spring-boot-testcontainers, testcontainers-postgresql, testcontainers-junit-jupiter.
- Config via env vars with local defaults; virtual threads on; `ddl-auto=validate`; `open-in-view=false`; only `health` actuator endpoint exposed.
- Flyway `V1__enable_postgis.sql`.
- Tests (Testcontainers, same images as Compose): health UP incl. db + redis; Flyway at v1 and PostGIS 3.5 available.
