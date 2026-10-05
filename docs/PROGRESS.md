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

## Step M1c — Name onboarding (2026-10-01)
- "What should we call you?" screen (`NameScreen` + `NameController`): trimmed 1–50 chars validated client-side, `PATCH /api/v1/me` via `AccountRepository.updateDisplayName`, backend ProblemDetail shown inline, "Not you? Sign out" escape.
- `SessionController.accountUpdated` swaps in the profile returned by the PATCH (no `/me` refetch).
- Route resolver: session ready + no display name → `/name` (so an interrupted onboarding resumes on next launch); with a name → `/profile`.
- Role choice deliberately not part of onboarding (ADR-019).
- Tests: 37 total (+6 name controller, +1 resolver); shared test fakes moved to `test/support/account_fakes.dart`.

## Step 3a — Venues & spaces CRUD (2026-10-01)
- Flyway V3: `venues` (owner, name, description, address, city, lat/lng with CHECKs, generated `location geography(Point,4326)`), `spaces` (capacity CHECK 1–1000, ON DELETE CASCADE), `space_gear` (ordered name/details). Indexes: `venues(owner_id)` for "my venues", `spaces(venue_id)` for venue detail/cascade. GiST index on `location` deferred to step 5 with the feed query.
- API (`venue` module, tag "Venues"): create (VENUE_ADMIN), get (any signed-in user), update/delete (owner), `GET /me/venues` (summary with space count via one DTO-projection query), space create/update/delete (owner). Non-owners get 404. PUT = full replacement; strings trimmed; blank optional text → null.
- Reads without N+1: venue + one entity-graph query for spaces with gear.
- `common.CurrentUser.id(jwt)` replaces per-controller helpers (MeController refactored).
- Spec regenerated; Dart client regenerated (`VenuesClient`, 6 new models) — no mobile screens yet (M2).
- Tests: 46 backend (+11 venue integration tests incl. generated geography value, DB CHECK, cascade, ownership 404, validation). Flyway test now asserts "no pending migrations" instead of a pinned version.
- Known follow-up: once bookings exist (step 4), deleting a venue/space with bookings must be blocked.

## Refactor — Layer sub-packages inside modules (2026-10-01)
- `auth`, `venue` and `common` split into `controller/ service/ repository/ model/ dto/ exception/ config/` (+ `auth/firebase/`, `common/web/`); tests mirror the layout. Moves done with `git mv` so history follows the files.
- Visibility widened only where a sub-package boundary required it: `FirebaseAdminTokenVerifier` (class + constructor) and `JwtService.ROLES_CLAIM` are now public. Controllers and config classes stay package-private.
- No behaviour or API change: 46/46 tests pass, `docs/api/openapi.yaml` byte-identical, real-Firebase boot verified. Convention added to CLAUDE.md §6.

## Fix — Align with the updated CLAUDE.md (2026-10-02)
- **Gear list removed (BYOI):** Flyway V4 drops `space_gear`; `GearItem`/`GearItemDto` deleted; spaces are name + capacity only. Spec and Dart client regenerated (stale generated files removed; regeneration now clears the folder first).
- **Role check moved to the service layer** (§6): `@PreAuthorize("hasRole('VENUE_ADMIN')")` now on `VenueService.create` instead of the controller.
- CLAUDE.md: commit-tag format `[area] - message`, no-AI-trailer rule and layer sub-package convention restored.
- Plan change: availability slots move to step 6 (events + slots) per the new build order; step 3's remainder is FSSAI number, hosting mode, sound policy + curfew, house rules, `verification_status`.
- Tests: 46 backend, 37 mobile — all pass.

## Step 3b — Venue policies & verification status (2026-10-02)
- Flyway V5: `fssai_number` (14-digit CHECK; placeholder `00000000000000` only for pre-existing dev rows, default dropped), `hosting_mode` (OPEN/SELF_ONLY), `sound_policy` (ACOUSTIC_ONLY/AMPLIFIED_ALLOWED, default acoustic), `sound_curfew` (local `time`, nullable), `time_zone` (default Asia/Kolkata, not exposed yet), `house_rules`, `verification_status` (PENDING/VERIFIED/REJECTED) + `rejection_reason`; partial unique index on `fssai_number` WHERE VERIFIED.
- `Venue`: `updateDetails` / `updatePolicies` / `changeFssaiNumber` — a new FSSAI number resets verification to PENDING and clears the rejection reason; other edits keep the status. Verification status never comes from a request.
- API: VenueRequest/Response + VenueSummary carry the new fields; curfew is `"HH:mm"` both ways (`@JsonFormat`).
- Fix to step 2c: `OpenApiSpecTest` now decodes the YAML as UTF-8 (MockMvc defaulted to ISO-8859-1 and corrupted `₹`, which broke Dart generation).
- Spec + Dart client regenerated (venue models + per-model enum types).
- Tests: 55 backend (+9 venue: defaults, optional curfew/rules, FSSAI format, unknown enums, status not settable, verification reset rule, partial uniqueness, DB CHECKs, summary status), 37 mobile.

## Change — Sound policy, curfew & house rules per space (2026-10-02)
- Design 8.4 puts them on the space ("Acoustic-only spaces can't host amplified events"); 3b had them on the venue. Moved before any event depends on them.
- Flyway V6: adds `sound_policy` (CHECK), `sound_curfew`, `house_rules` to `spaces`, copies each venue's values into its spaces, drops them from `venues`. Copy verified by a rolled-back dry run on seeded V5 data. Hosting mode and `time_zone` stay on the venue.
- API: moved from VenueRequest/Response to SpaceRequest/Response (`soundPolicy` required, curfew `HH:mm` optional, house rules optional). Spec + Dart client regenerated. CLAUDE.md §1/§5.7 wording updated.
- Tests: 55 backend (space-level policy round trip incl. two spaces with different policies, missing/unknown policy → 400, DB CHECK on spaces), 37 mobile.

## Step 4a — Host profiles (2026-10-02)
- Flyway V7: `host_profiles` (one per user via UNIQUE owner; type INDIVIDUAL/GROUP; group kind BAND/FRIENDS/COMMUNITY + name; bio ≤200; area; Instagram handle CHECK) with CHECKs that a group always has kind + name and an individual never does; `host_profile_genres`; ordered `host_media_links` (http(s) CHECK).
- `common.model.Genre`: fixed list shared with future event tags / feed filters.
- New `host` module: `PUT /api/v1/me/host-profile` (HOST role, checked in the service; create-or-replace), `GET /api/v1/me/host-profile` (404 until created), `GET /api/v1/host-profiles/{id}` (any signed-in user). Individual display name = owner's display name (via auth's UserService); group fields ignored for individuals. Instagram stored without "@". Up to 5 genres and 5 links.
- Spec + Dart client regenerated (`HostsClient`).
- Tests: 64 backend (+9 host profile: role 403, group round trip with link order + sorted genres, individual name, group validation, replace/switch type, public read + 404, invalid fields, DB uniqueness + group CHECK), 37 mobile.

## Step 4b — Host group members (2026-10-03)
- Flyway V8: `host_group_members` (PK profile+user, status INVITED/ACCEPTED, accepted_at CHECK, index on user_id) and `host_profiles.member_count` (CHECK 0–9).
- Cap of 10 people (owner + 9 invited/accepted) enforced atomically: `UPDATE … SET member_count = member_count + 1 WHERE member_count < 9`; insert-if-absent for the invite (`ON CONFLICT DO NOTHING`), and a failed insert rolls back the reserved slot. Accept is a conditional update (INVITED → ACCEPTED); decline/leave/remove/cancel delete the row and release the slot in the same transaction. `member_count` is mapped read-only on the entity so saving a profile can't overwrite it.
- Endpoints (tag Hosts): owner — invite by phone (registered users only; 404 "ask them to sign up"), list pending invites (masked phone, no name), remove member / cancel invite; invitee — my invites (group name, kind, inviter), accept, decline; member — leave. Public profile lists owner (admin) + accepted members.
- Group → individual switch blocked (409) while members or invites exist.
- auth: `UserService.findByPhone` and bulk `summaries(ids)` (one query for member names).
- Spec + Dart client regenerated.
- Tests: 72 backend (+8 membership incl. 15 concurrent invites → exactly 9 accepted by the cap), 37 mobile.

## Step 5a — Platform admin + venue verification (2026-10-05)
- Flyway V9: `PLATFORM_ADMIN` in the role CHECK; `venues.verification_requested_at` + queue index; `admin_actions` (append-only log, keyset index).
- `PLATFORM_ADMIN` only from the `WEJAM_ADMIN_PHONES` allow-list, synced at every login (granted if listed, revoked if not). `Role.isSelfAssignable` is now an explicit allow-list (HOST, VENUE_ADMIN) — the old `!= USER` would have made the new role self-assignable.
- New `verification` module (queue, review, approve, reject with reason) and `admin` module (action log, `GET /admin/actions` keyset-paginated). Status writes stay in `VenueService` as conditional updates (`… WHERE verification_status = 'PENDING'`); approving a second venue with an already-verified FSSAI → 409 (partial unique index caught). Owner `POST /venues/{id}/resubmit` (REJECTED → PENDING). Automated checks: offline FSSAI structure heuristic + count of other venues with the same number.
- Admin role checked in services (§6) and by a `/api/v1/admin/**` rule in SecurityConfig. Log writes use `Propagation.MANDATORY` so a decision and its log entry commit together.
- `Venue` is `@DynamicUpdate` so an owner's edit never rewrites a verification decision with a stale value.
- Spec + Dart client regenerated (`AdminClient`). The new `PLATFORM_ADMIN` enum value broke the app's exhaustive role switch at compile time (caught by the regeneration); app `Role` gains `platformAdmin` + profile chip label.
- Tests: 84 backend (+8 verification incl. concurrent approve/reject, +2 allow-list grant/revoke, +1 PLATFORM_ADMIN not self-assignable, +1 phone re-link), 37 mobile.
- Fix (bug from 2a): a Firebase account recreated with the same phone gets a new uid, so the exchange hit `users.phone` UNIQUE → 500 on every login. Login now re-links the existing user to the new uid when the OTP-verified phone matches (`relinkPhoneToFirebaseUid`); regression test added.
- Dev setup: `backend/.env` (gitignored) + committed `backend/.env.example`, loaded via `spring.config.import=optional:file:./.env[.properties]` — no more `export` lines; real env vars still override. New `wejam.firebase.credentials-file` (from `GOOGLE_APPLICATION_CREDENTIALS`) because the Google SDK only reads that variable from the OS environment. Verified booting with an empty environment in real Firebase mode.

## Step 5b — Object storage + venue verification documents (2026-10-05)
- Infra: MinIO in Compose (API 9000, console 9001) + a one-shot `minio-init` that creates the private bucket `wejam-private`. Uses Chainguard's MinIO build (`cgr.dev/chainguard/minio`) because `minio/minio` is no longer pullable from Docker Hub. The image has no shell, so readiness is awaited by `mc` in the init container instead of a healthcheck.
- Backend deps: AWS SDK v2 BOM 2.55.11 + `s3`; test: `testcontainers-minio`.
- `common/storage`: `ObjectStorage` over `S3Client` + `S3Presigner` (path-style). Presigned PUT signs Content-Type and Content-Length; presigned GET for downloads (5 min); `deleteAfterCommit` removes objects only once the DB transaction commits. Separate `STORAGE_ENDPOINT` (backend → MinIO) and `STORAGE_PUBLIC_ENDPOINT` (the host the phone uses — the signature covers the host, so URLs must be signed for it).
- Flyway V10: `verification_documents` (owner VENUE/HOST_PROFILE, type FSSAI_CERTIFICATE/LEASE_AGREEMENT, jpeg/png/pdf, ≤ 10 MB, PENDING_UPLOAD → UPLOADED, one current document per owner+type via a partial unique index); `venues.verification_issues text[]`.
- New endpoints under `/api/v1/venues/{id}/documents`: start upload (returns URL + headers to send), confirm (HEADs the object and checks size + type), list (with short-lived download URLs), delete. Only the venue owner, only while PENDING/REJECTED (VERIFIED → 409). Uploading a type again replaces the old document and deletes its file.
- Admin review now includes the venue's documents. Reject accepts optional `issues` (FSSAI_NUMBER, FSSAI_CERTIFICATE, LEASE_AGREEMENT, VENUE_DETAILS) shown to the owner as `verificationIssues`; cleared on approve, resubmit and FSSAI change.
- Venue delete publishes `VenueDeletedEvent`; the verification module deletes its rows in the same transaction and the files after commit (venue module doesn't know documents exist).
- Spec + Dart client regenerated (new `Document*` models, `verificationIssues`, `issues`). No app UI yet.
- Tests: 92 backend (+8 document tests doing real HTTP uploads to MinIO: round trip with byte-for-byte download, storage rejects wrong size/type, declaration validation, replace, delete, owner/verified guards, admin review + issues + resubmit, venue-delete cleanup), 37 mobile.
- Known gap: uploads that are started but never confirmed leave a `PENDING_UPLOAD` row (and maybe an object). A cleanup sweep is planned with 5c's scheduled jobs.
