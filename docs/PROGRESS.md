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

## Step 2b — Real Firebase ID token verification (2026-09-29)
- Dep: `com.google.firebase:firebase-admin` 9.11.0 (pinned via `firebase-admin.version`; not managed by Boot). Full transitive set — see ADR-011.
- `FirebaseConfig` (active unless `wejam.auth.fake-firebase=true`): `FirebaseApp` from `GOOGLE_APPLICATION_CREDENTIALS` + `FIREBASE_PROJECT_ID` (validated, startup fails if blank), deleted on shutdown for DevTools restarts.
- `FirebaseAdminTokenVerifier`: `verifyIdToken(token, checkRevoked=true)`; uid + `phone_number` → `FirebaseIdentity`. Token/user problems → 401; transport/cert-fetch failures → 503 (`FirebaseUnavailableException`) so clients retry instead of signing out.
- Service-account key lives at `~/.config/wejam/firebase-sa.json` (chmod 600); `.gitignore` guards against key files in the repo.
- Tests: unit test of the verifier's mapping with mocked `FirebaseAuth` (valid, missing phone, 5 rejection codes, malformed, network/cert failure → 503). Integration tests stay on the fake verifier.
- Manually verified: startup fails without project id; real mode rejects fake-format, garbage and forged RS256 tokens with 401. End-to-end via Firebase Auth REST (test number +911234567890 / 123456): real ID token → app JWT → `/me`; after `revokeRefreshTokens` the same ID token → 401. Requires SMS region policy allowing India.

## Step 2c — OpenAPI spec + Swagger UI (2026-09-30)
- Dep: `org.springdoc:springdoc-openapi-starter-webmvc-ui` 3.1.1 (pinned via `springdoc.version`).
- `common/OpenApiConfig`: title/version, fixed server `http://localhost:8080`, global `bearerAuth` (HTTP bearer JWT); `POST /api/v1/auth/token` opts out.
- `@Tag("Auth")`/`@Tag("Me")` → Dart `AuthApi`/`MeApi`; DTOs annotated with required/nullable for Dart null-safety.
- springdoc config: OpenAPI 3.0, keys sorted, only `/api/**` documented, `application/json` responses; `SPRINGDOC_ENABLED=false` hides docs + UI.
- Security: `/v3/api-docs`, `/v3/api-docs/**`, `/v3/api-docs.yaml`, `/swagger-ui/**`, `/swagger-ui.html` public.
- `OpenApiSpecTest` regenerates `docs/api/openapi.yaml` on every `./mvnw test` and asserts operations, security and schema nullability; output verified deterministic and identical to the live app's spec.
