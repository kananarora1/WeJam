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

## ADR-014: Android-first, physical device over wireless debugging
- **Context:** No Xcode; ~17 GB free disk; emulator fell back to software rendering under memory pressure.
- **Decision:** Only the `android` platform is generated for now; develop on a physical phone via wireless adb, emulator (`wejam_pixel`) as backup. iOS added later with `flutter create --platforms ios .`.
- **Why:** Lightest setup with hot reload; avoids a ~15 GB Xcode install. The backend must be reached via the Mac's LAN IP (not `10.0.2.2`), so the base URL will be configurable (M1b).

## ADR-015: Navigation derived from state (go_router redirect)
- **Context:** Auth screens need to move between splash, phone, OTP and home as Firebase/session state changes (including auto-verification and session restore).
- **Decision:** A single `redirect` computes the target route from `authStateProvider` and `PhoneAuthState.awaitingCode`; a `ValueNotifier` bumped by `ref.listen` triggers re-evaluation. Screens only change state, never call `context.go`.
- **Why:** One place defines where a user may be; impossible to reach OTP without a pending verification or phone screens while signed in; no duplicated navigation on rebuilds.

## ADR-016: Firebase client config committed — SUPERSEDED by ADR-017
- **Context:** `flutterfire configure` generates `lib/firebase_options.dart` and `android/app/google-services.json` containing the project id, app id and a Firebase API key.
- **Decision:** Commit them.
- **Why:** They are public client identifiers shipped inside every APK, not secrets (the service-account key stays in `~/.config/wejam`). Committing lets the app build right after clone. Follow-up before any public release: restrict the API key to the Android package + SHA in Google Cloud.

## ADR-017: Firebase client config not committed; API key restricted
- **Context:** ADR-016 committed `firebase_options.dart` / `google-services.json`. After pushing, GitHub secret scanning flagged the Firebase API key; it was unrestricted, so anyone could call the project's Firebase APIs with it.
- **Decision:** Both files are gitignored and untracked; each developer generates them with `flutterfire configure` (mobile/README.md). The leaked key is rotated: new key restricted to the Android app (package + SHA-1) and to the Identity Toolkit, Token Service and Firebase Installations APIs; old key deleted. History is not rewritten — the old key is dead after rotation.
- **Why:** Although Firebase client keys ship inside every APK, an unrestricted key in a public repo invites quota abuse, and secret-scanning alerts should not be normalised. Restriction limits what a copied key can do; not committing avoids the next alert.

## ADR-018: Generated Dart client via swagger_parser; app JWT in memory only
- **Context:** §7 contract flow requires a Dart client generated from `docs/api/openapi.yaml`; the app needs the backend JWT on every call and must survive its 1h expiry (ADR-005).
- **Decision:** `swagger_parser` (pure Dart) generates Retrofit/Dio clients + json_serializable models into `lib/core/api/generated/`, committed so the app builds after clone. The app JWT lives only in memory (`AppTokenStore`); on cold start and on any 401 the app re-exchanges a (force-refreshed) Firebase ID token, once, then retries once. Domain types (`Account`, `Role`) wrap the generated DTOs.
- **Why:** No Java/Node toolchain for generation; null-safety comes straight from the spec's required/nullable annotations. Not persisting the JWT means nothing sensitive of ours is stored on the device; Firebase already persists its own session. Wrapping DTOs keeps regenerated code from rippling through the UI.

## ADR-019: Onboarding asks only for a name; roles are offered when needed
- **Context:** Wireframe 1.4 combines name and "I'm here to" (play or listen / host / run a venue) on one screen.
- **Decision:** Onboarding collects only the display name. HOST / VENUE_ADMIN will be offered where they first matter (e.g. "Start hosting" / "Run a venue" from Profile, in the venue/host steps). `POST /me/roles` already supports this.
- **Why:** Shortest path into the app; most users only play or listen. Asking for a role before the user has seen what hosting or venue admin involves adds a decision with no context.

## ADR-020: Venue location as lat/lng plus a Postgres-generated geography column; one location per venue
- **Context:** The feed (step 5) needs `geography(Point, 4326)` for `ST_DWithin`; CRUD only needs to read/write coordinates. The wireframe shows a pin per space.
- **Decision:** Store `latitude`/`longitude` as plain columns mapped by JPA; `location` is `GENERATED ALWAYS AS (ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography) STORED` and not mapped. Location lives on the venue, not on each space.
- **Why:** No Hibernate Spatial dependency or geometry type-mapping quirks under `ddl-auto=validate`; the geography value can never drift from the coordinates; range CHECKs guard bad input at the DB. Spaces of one venue share an address, and a single point per venue keeps the feed simple.

## ADR-021: Layer sub-packages inside each feature module
- **Context:** Flat module packages grew to ~20 classes (`auth`), mixing controllers, config, entities and exceptions.
- **Decision:** Keep packaging by feature at the top level (§4), and inside each module use one sub-package per layer (`controller`, `service`, `repository`, `model`, `dto`, `exception`, `config`, plus focused ones like `auth/firebase`).
- **Why:** Easier to navigate. Trade-off: package-private encapsulation now only works within a layer, so a few cross-layer members had to become public; anything not needed across sub-packages stays package-private.

## ADR-022: No venue gear inventory (BYOI)
- **Context:** Step 3a modelled a per-space gear list (from wireframe 6.4). The updated product brief says target venues almost never provide instruments or sound gear: hosts bring the setup, performers bring their instruments.
- **Decision:** Remove the gear list entirely (Flyway V4 `DROP TABLE space_gear`, API fields removed). Equipment is described per event by the host (`event.host_setup`, `event.amplified`) and per participant (`bringing_own_instrument`) in later steps.
- **Why:** Avoids a flow that depends on venue-provided gear (explicitly out of scope). A forward migration rather than editing V3, because V3 is already applied in existing databases.

## ADR-023: FSSAI uniqueness only among verified venues; a new number resets verification
- **Context:** The FSSAI license identifies one food business premises and is the basis of venue verification (§5.12). A plain UNIQUE constraint lets anyone block a café by registering its number first.
- **Decision:** Partial unique index `ON venues (fssai_number) WHERE verification_status = 'VERIFIED'`. Changing a venue's FSSAI number sets it back to PENDING and clears the rejection reason; other edits don't touch verification. Status is only changed by the platform admin (step 5).
- **Why:** Squatting can't block the real owner — the admin resolves duplicates — yet the DB still guarantees one license is verified for at most one venue. Tying verification to the number prevents swapping in an unverified license after approval.

## ADR-024: Sound policy, curfew and house rules belong to a space
- **Context:** Step 3b stored them on the venue (CLAUDE.md §5.7 wording). The design (8.4 Space · edit) sets them per space: a rooftop may be acoustic-only for the neighbours while the main floor allows amplified sound.
- **Decision:** Move them to `spaces` (Flyway V6, copying existing venue values to every space of that venue). Hosting mode stays venue-wide; the curfew is a local time interpreted in the venue's `time_zone`. Step 6 validates amplified events and end times against the event's space.
- **Why:** Matches how venues actually work and the agreed design; cheap now (no events reference it yet), costly after step 6. Supersedes the sound-policy part of step 3b.

## ADR-025: One host profile per user; fixed genre list; members are registered users invited by phone
- **Context:** Step 4 (host profiles, design 4.1–4.4). The design shows group members invited "by phone or @username"; users have no usernames.
- **Decision:** One `host_profiles` row per user (individual or group). Genres are a fixed enum in `common` (shared with events/feed later), max 5. Media links are URLs only, max 5. In 4b, group members are invited by the phone number of an already-registered user and must accept; no usernames, no invites to non-users.
- **Why:** Keeps identity = phone OTP (§5.12) without inventing a username system; inviting non-users would need SMS (paid, ADR-012) or share links (out of scope). A fixed genre list makes feed filters and event tags match exactly. A second profile per user (solo + band) is a v2 concern.

## ADR-026: Group membership via conditional writes and an atomic member counter
- **Context:** Group members (step 4b) with a cap of 10 people; invites, accepts and removals can race (double taps, two devices, many invites at once).
- **Decision:** `host_profiles.member_count` counts invited + accepted members and is incremented with `WHERE member_count < 9` (0 rows = full), per §5.4. Invite insert uses `ON CONFLICT DO NOTHING`; accept is `UPDATE … WHERE status = 'INVITED'`; every delete releases the slot in the same transaction. Pending invites count toward the cap and show only a masked phone to the owner. Unknown numbers get an explicit 404 (accepted enumeration trade-off); switching a group with members to individual is rejected.
- **Why:** No read-then-write anywhere, so concurrency can't exceed the cap or double-accept (proven by a 15-thread test). Counting pending invites keeps the cap meaningful; masking avoids revealing who owns a number before they accept.

## ADR-027: Platform admins from an allow-list; verification as conditional transitions with an audit log
- **Context:** Step 5a needs a `PLATFORM_ADMIN` role, a venue review workflow and a record of every admin decision (design 9.x; CLAUDE.md §5.12, §5.17).
- **Decision:** Admins are configured by phone number (`WEJAM_ADMIN_PHONES`); the role is granted/revoked at each login and can never be self-assigned. Verification transitions are single conditional UPDATEs (`PENDING → VERIFIED|REJECTED` by an admin; `REJECTED → PENDING` by the owner; any → PENDING on FSSAI change). Each decision writes an `admin_actions` row in the same transaction (`Propagation.MANDATORY`). `Venue` uses `@DynamicUpdate` so owner edits only write changed columns.
- **Why:** No API path can create an admin; removing a number from config revokes access on the next login. Conditional updates make concurrent decisions safe (one winner, 409 for the other) without locks. The log can't drift from the decisions it describes. `@DynamicUpdate` closes a lost-update hole between owner edits and admin decisions on the same row.
