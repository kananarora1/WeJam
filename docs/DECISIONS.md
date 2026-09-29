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

## ADR-005: App JWT via Spring Security resource server, HS256, no refresh tokens
- **Context:** Firebase handles phone OTP; backend needs its own token carrying app roles.
- **Decision:** Exchange Firebase ID token for a 1h HS256 JWT (`sub`=user id, `roles` claim) signed with `JWT_SECRET`. No refresh tokens — the client re-exchanges a fresh Firebase ID token on expiry.
- **Why:** Single issuer/verifier, so a symmetric key suffices. Re-exchange avoids a refresh-token store and rotation. Trade-off: role changes/bans apply only when the current JWT expires (max 1h) — except self-assigned roles, which return a new token immediately.

## ADR-006: Self-service HOST / VENUE_ADMIN roles
- **Context:** No admin panel exists to grant roles.
- **Decision:** `POST /api/v1/me/roles` lets any user add HOST or VENUE_ADMIN to themselves; USER is implicit at first login.
- **Why:** Portfolio app; keeps onboarding simple. Revisit if venue verification is ever needed.

## ADR-007: Race-safe user upsert with native `ON CONFLICT`
- **Context:** Two first logins for the same Firebase uid can race.
- **Decision:** `INSERT … ON CONFLICT (firebase_uid) DO NOTHING`, then read. Role inserts also use `ON CONFLICT DO NOTHING`.
- **Why:** Atomic in the DB, no exception-driven retry. Catching a unique violation doesn't work inside a Postgres transaction because the transaction is aborted after the error.

## ADR-008: Fake Firebase verifier behind a property
- **Context:** Tests and local dev need logins without a real Firebase project / phone.
- **Decision:** `FakeFirebaseTokenVerifier` accepts `fake:<uid>[:<phone>]`, active only when `wejam.auth.fake-firebase=true` (default false). Logs a WARN when active.
- **Why:** Deterministic tests and curl-able local dev. Off by default, so production can't accidentally accept fake tokens.

## ADR-009: Interleave backend and mobile steps
- **Context:** Original order built all backend features (steps 1–6) before any Flutter work.
- **Decision:** After auth (2a–2c), alternate: backend feature → Flutter screens that consume it (M1–M5). Notifications, live lineup and load test follow.
- **Why:** Learn Flutter incrementally (setup + login first, not setup + auth + map at once), and exercise each API with a real client while it's fresh. Trade-off: more context switching; API changes may require client regeneration in the same step.
