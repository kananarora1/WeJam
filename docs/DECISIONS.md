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

## ADR-010: Firebase Admin SDK with revocation check
- **Context:** Firebase ID tokens can be verified either with a generic JWT library against Google's public keys, or with the Firebase Admin SDK.
- **Decision:** Admin SDK, `verifyIdToken(token, checkRevoked = true)`. Credentials via `GOOGLE_APPLICATION_CREDENTIALS` (key file outside the repo).
- **Why:** Official, maintained verification logic; revocation check also rejects disabled users and revoked sessions (one Firebase call per exchange ≈ once/hour/user). Same credentials will serve FCM in step 7. Cost: large dependency tree and a service-account secret to manage.

## ADR-011: No exclusions on firebase-admin's transitive dependencies
- **Context:** Tried excluding `google-cloud-firestore`/`google-cloud-storage` (~71 jars incl. gRPC, OpenTelemetry, protobuf).
- **Decision:** Keep the SDK's full dependency set.
- **Why:** The SDK itself needs classes that only arrive via those modules (`gax` paging types, `google-http-client-jackson2` for `FirebaseOptions`); failures surface only at runtime (`NoClassDefFoundError`). Re-adding pieces one by one is fragile across SDK upgrades. Revisit only if artifact size/startup becomes a real problem.

## ADR-012: Phone OTP with test numbers during development; Google Sign-In later
- **Context:** Real SMS OTP requires Firebase Blaze (paid per SMS); no free real-SMS option exists (carrier costs; DLT registration for Indian providers).
- **Decision:** Develop with Firebase test phone numbers on the free Spark plan. Add Google Sign-In later as the free real-user login. Enable Blaze + budget alert only if real phone OTP is needed for a demo.
- **Why:** Zero cost now. Any Firebase sign-in method yields a Firebase ID token, so the backend exchange (2b) needs no changes; `users.phone` is already nullable for non-phone logins.

## ADR-013: OpenAPI spec generated by an integration test
- **Context:** CLAUDE.md §7 requires the spec committed at `docs/api/openapi.yaml` and updated whenever the API changes.
- **Decision:** `OpenApiSpecTest` fetches `/v3/api-docs.yaml` from the test context and writes the file on every test run. Output made deterministic (sorted keys, fixed server URL, only `/api/**`). OpenAPI 3.0 for Dart generator compatibility.
- **Why:** Reuses the existing Testcontainers context — no running app or env vars needed (unlike `springdoc-openapi-maven-plugin`). API changes show up in `git status` after `./mvnw test`, so the spec can't silently drift.
