# Progress

Log of completed steps (what was built, key decisions). Newest at the bottom.

## Step 1 — Monorepo skeleton, local infra, backend boot-up (2026-09-29)
- Monorepo layout: `backend/` (Maven, Spring Boot 4.1.1), `mobile/` (placeholder), `infra/`, `docs/`; combined root `.gitignore`.
- `infra/docker-compose.yml`: PostGIS 17-3.5 (host port 5433) + Redis 7, healthchecks, named volume.
- Backend deps added: actuator, data-jpa, data-redis, flyway (+ postgresql module), postgresql driver; test: spring-boot-testcontainers, testcontainers-postgresql, testcontainers-junit-jupiter.
- Config via env vars with local defaults; virtual threads on; `ddl-auto=validate`; `open-in-view=false`; only `health` actuator endpoint exposed.
- Flyway `V1__enable_postgis.sql`.
- Tests (Testcontainers, same images as Compose): health UP incl. db + redis; Flyway at v1 and PostGIS 3.5 available.

## Step 2a — App JWT, Spring Security, users & roles (2026-09-29)
- Deps: `spring-boot-starter-security-oauth2-resource-server` (JWT encode/decode + bearer filter), `spring-boot-starter-validation`.
- Flyway `V2__users.sql`: `users` (uuid PK, unique `firebase_uid`, unique `phone`) + `user_roles` (PK `(user_id, role)`, CHECK on role values).
- `POST /api/v1/auth/token`: Firebase ID token → upsert user (`INSERT … ON CONFLICT DO NOTHING`, USER role on creation) → HS256 app JWT (`sub`=user id, `roles` claim, 1h TTL).
- `GET /api/v1/me`, `PATCH /api/v1/me` (display name, 1–50 chars, stored trimmed; collected on a post-OTP "complete profile" screen when null), `POST /api/v1/me/roles` (self-assign HOST/VENUE_ADMIN, idempotent, returns a fresh JWT).
- `FirebaseTokenVerifier` interface; only a fake implementation (`wejam.auth.fake-firebase=true`) until 2b.
- `common/GlobalExceptionHandler`: ProblemDetail for all MVC errors + `errors` map for validation failures.
- Health: anonymous sees only status; authenticated sees component details.
- Tests: exchange, idempotent re-login, concurrent first logins, invalid/blank token, /me with missing/tampered/expired/valid token, role add (hasRole check, idempotency, USER/unknown rejected).
- Known gap: OpenAPI spec not generated yet (step 2c).
