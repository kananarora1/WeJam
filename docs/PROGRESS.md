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

## Step M1a — Flutter project, design system, Firebase phone login (2026-09-30)
- `flutter create` in `mobile/` — Android only for now; application id `com.kananarora.wejam`; app label "WeJam". Android launch screen themed dark (`#141110`, no icon on Android 12+) so launch → Flutter splash has no white flash.
- Deps: `flutter_riverpod` 3.4, `go_router` 18, `firebase_core` 4.15, `firebase_auth` 6.7.
- Design system from the wireframe (Foundations): `AppTokens` ThemeExtension (dark colors) + `AppSpace`/`AppRadii`/`AppMotion`/`AppFonts` constants; `buildDarkTheme()` type scale (Instrument Serif display, Jost UI). Fonts bundled as assets (OFL licences included). IBM Plex Mono dropped (not used on app screens).
- Shared widgets: `PrimaryButton` (amber pill, EQ-bars loading state), `EqBars` (freezes under reduced motion).
- Auth feature: `PhoneAuthService` interface + `FirebasePhoneAuthService` (callback API → Future, Firebase error codes → friendly copy, raw error logged via `debugPrint`); `PhoneAuthController` (Notifier) for phone → code flow; `authStateProvider` (StreamProvider over Firebase auth state).
- Screens: Splash (cold start only: wordmark focuses in from a blur, holds, waits for the session check, then pops out toward the viewer; the router leaves the splash only when it reports completion), Phone (1.2), OTP (1.3: six boxes, auto-submit, 30 s resend cooldown, back = change number), interim "You're in" + sign out.
- go_router with state-derived redirects (splash / phone / otp / home); screens never navigate imperatively. All routes cross-fade (the platform zoom fought the splash pop-out).
- Animations judged on a release build: debug (JIT) builds start ~2–3 s slower and stall on first build of each screen.
- Firebase: Android app registered via `flutterfire configure` (`firebase_options.dart`, `google-services.json`); debug SHA-1/SHA-256 registered.
- Tests: 8 controller unit tests (fake service), 3 OTP widget tests. Verified on a physical Android 16 phone with test number +91 1234567890 / 123456.

## Fix — Firebase API key exposure (2026-09-30)
- GitHub flagged the Firebase Android API key committed in M1a (`firebase_options.dart`, `google-services.json`).
- Both files gitignored and untracked; setup documented in `mobile/README.md` (replaces the Flutter template README). ADR-017 supersedes ADR-016.
- Key rotation and restriction done in Google Cloud console (manual).
- Removed a stray duplicate branch ref `main 2` (created when the Desktop folder was moved; pointed at an ancestor of main).

## Step M1b — App ↔ backend session (2026-09-30)
- Deps: `dio`, `retrofit`, `json_annotation`; dev: `swagger_parser`, `build_runner`, `retrofit_generator`, `json_serializable`.
- Generated client from `docs/api/openapi.yaml` (`mobile/swagger_parser.yaml` → `lib/core/api/generated/`, committed incl. `.g.dart`). Backend API unchanged.
- `ApiConfig.baseUrl` from `--dart-define=API_BASE_URL` (default `http://10.0.2.2:8080` for the emulator). Cleartext HTTP allowed in debug/profile manifests only.
- `AppTokenStore` (app JWT in memory only), `AuthInterceptor` (Bearer header; on 401 one re-authentication + one retry; token-exchange path excluded), `ApiFailure` (network / ProblemDetail / 5xx → user copy).
- `AccountRepository` (exchange, me → domain `Account`/`Role`), `SessionController` (AsyncNotifier: Firebase uid → force-refreshable ID token → app JWT → `/me`; shared in-flight re-auth; signs out when a fresh token is rejected; Riverpod auto-retry disabled).
- Routing extracted to pure `resolveRoute` (splash / phone / otp / loading / session-error / profile). New screens: "Getting you in…", "That didn't land" (Try again / Sign out), minimal Profile (name, phone, role chips, sign out). Interim signed-in screen removed.
- `PhoneAuthService.getIdToken({forceRefresh})` added.
- Tests: 30 total (+8 route resolver, +7 session controller, +5 interceptor with a scripted Dio adapter).
