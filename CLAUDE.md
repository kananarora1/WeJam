# CLAUDE.md — Jam App (working name)

This file is the standing context + working agreement for this repo. Read it fully at the start of every session and follow it strictly.

---

## 1. What we're building

A mobile app for **live jam nights at cafés/restaurants** — mainly new or growing cafés that want footfall and reach. Built as a **learning + portfolio project** (not a product yet — no growth, payments, or monetization work).

**Key reality: BYOI (bring your own instrument).** Target venues almost never provide instruments or sound gear. The **host** brings the setup (speaker/PA, mics, maybe a cajón); **performers bring their own instruments**. The venue provides space, timing, food & drinks, a sound policy, and approval. Never design a flow that depends on venue-provided gear.

**Actors / roles**
- **Venue admin** (`VENUE_ADMIN`) — gets the venue **verified** (FSSAI); lists spaces + availability slots; sets **hosting mode** (`OPEN` / `SELF_ONLY`) for the venue and **sound policy**, curfew and house rules per space; approves/rejects host requests; can also **create events itself**; can report hosts; sees venue analytics.
- **Host** (`HOST`) — an **individual or a group** (band, friends' group, community); profile with Instagram + performance video links; optionally **verified** (college/company ID); requests a slot and creates an event in one of the 4 formats; declares their setup; sets questions for participants; manages participants.
- **User** (`USER`) — discovers nearby events; joins as performer or audience depending on the format; answers host questions; rates/reports after the event.
- **Platform admin** (`PLATFORM_ADMIN`) — reviews verification documents, handles reports, suspends/unsuspends accounts. Admin-only endpoints (+ Swagger, or a minimal admin section later).

### Event formats (v1) — host/venue picks exactly ONE per event

| Format | Real case | Performers | Audience |
|---|---|---|---|
| `OPEN_JAM` | Acoustic circle, sing-along, beginner jams | Anyone joins **instantly**, picks instrument. No instrument caps. | Yes, capped |
| `SHOWCASE` | Band/group performs, people enjoy (concert style, house-band sing-along) | **Host group only** — nobody joins as performer | Yes, capped (main purpose) |
| `CALL_FOR_MUSICIANS` | "Have guitar + vocals, need a drummer and bassist — bring your instruments" | Host lists **instrument needs with counts**; users **apply** for a needed instrument → host **approves/rejects** | Optional (host toggles), capped |
| `OPEN_MIC` | Sign up, get a timed turn | Users **sign up for a turn** (turn length set by host, total turns capped) → ordered lineup | Yes, capped |

`Event.type` stays generic for the future (`JAM | OPEN_MIC | SHAYARI | STANDUP ...` — `OPEN_MIC` format is the bridge to shayari/standup).

**Core flow:** Venue registers → platform admin verifies → venue publishes spaces/slots, hosting mode, sound policy → host requests a slot with event details (or venue self-creates) → venue approves → users join per format rules → live lineup/check-in on event night → completed → 2-way ratings / reports.

### In scope (v1)
1. Auth with roles: `USER`, `HOST`, `VENUE_ADMIN`, `PLATFORM_ADMIN`
2. Venue + space CRUD — venue: location, FSSAI number, photos, **hosting mode** (`OPEN` / `SELF_ONLY`); space: capacity, availability slots, **sound policy** (`ACOUSTIC_ONLY` / `AMPLIFIED_ALLOWED`) + **sound curfew time** (venue's time zone), **house rules** (free text: age limit, min spend, etc.)
3. Verification: venue (FSSAI + optional lease/owner agreement upload), host (optional college/company ID); reviewed by platform admin
4. Host profiles: individual/group, members, Instagram, performance video links
5. Events in the 4 formats; slot request → approve/reject (state machine); venue self-created events skip approval
6. Event details: format, tags (genre + "beginner friendly"), host setup (free text), whether amplified, fee info (performer fee, audience fee, perks — **info only**), up to 3 host questions
7. Nearby events feed (map + list, filter by date/genre/format) — all events public
8. Join flows per format (instant join / apply + approve / turn sign-up / audience), "bringing my own instrument" flag, answers to host questions
9. Live lineup/queue on event night (Open Mic turns; performer order for others) + check-in
10. Push notifications (approval, application accepted/rejected, reminder, "you're up next")
11. Post-event 2-way ratings + reports ("complaint centre") with admin moderation
12. Venue analytics from app data (RSVPs, check-ins, attendance rate, repeat attendees)

### v2 (designed-for, NOT built now — don't add scaffolding for these)
Hybrid/multi-segment events, private/invite-only events, invite-only venues + trusted hosts + auto-approve, venue "looking for a host" open calls, recurring series, co-hosts, venue staff roles / multi-outlet, instrument lending, song requests.

### Out of scope
Payments, chat, recommendations, web app, shareable web links, real third-party verification APIs (FSSAI/DigiLocker mocked/manual), self-hosted video, POS/sales data, venue-provided gear inventory.

---

## 2. About me (the developer) — how to work with me

- I'm a **Java/Spring backend developer**. Backend concepts can be explained concisely.
- I have **zero Flutter/Dart experience**. For mobile code, explain Flutter/Dart/Riverpod concepts the first time they appear (briefly, with the Java analogy where one exists). Don't re-explain once covered.
- I prefer **direct, dense, structured** explanations. No fluff, no long preambles.
- **You write the code. I test it, read it, ask questions, and may suggest optimisations.** My understanding of the code matters as much as the code working.

---

## 3. Working agreement (MOST IMPORTANT)

We build **one small step at a time**. Never implement multiple features or jump ahead.

For every step, follow this loop:

1. **Plan** — State the step's goal, files you'll create/change, key design decisions and trade-offs. Keep it short. **Wait for my go-ahead** before writing code.
2. **Implement** — Only what the approved step covers. Small, reviewable diff. No "while I'm here" extras, no speculative abstractions, no scaffolding for future features.
3. **Explain** — After coding: what each file/class does, why it's designed that way, anything non-obvious (concurrency, SQL, Flutter rebuild behaviour, etc.).
4. **How to test** — Exact commands / curl / steps / expected output. Include automated tests where the step warrants them.
5. **Stop.** Wait for me to test and ask questions. Do not start the next step until I say so.

Rules:
- If a step is too big, **propose splitting it** instead of doing it all.
- If you disagree with my suggestion or see a better approach, **say so with reasoning** — don't silently comply or silently deviate.
- If something is ambiguous, **ask** instead of assuming.
- If you need to change something from an earlier step, call it out explicitly and explain why.
- After each completed step, append an entry to `docs/PROGRESS.md` (what was built, key decisions) and any important decision to `docs/DECISIONS.md` (short ADR-style: context → decision → why).
- Never add dependencies without mentioning them and why.

---

## 4. Tech stack

### Backend
- **Java 21**, **Spring Boot** (version as already set up in `backend/`; virtual threads enabled), **Maven** (`./mvnw`)
- **Modular monolith** — packages by feature/module: `auth`, `venue`, `host`, `verification`, `event`, `participation`, `lineup`, `feedback` (ratings + reports), `analytics`, `notification`, `admin`, `common`. No microservices.
- **PostgreSQL + PostGIS** (Spring Data JPA + Hibernate Spatial where useful; native SQL where clearer)
- **Flyway** for all schema changes (never `ddl-auto=update`)
- **Redis** — feed cache, live lineup queue, pub/sub fanout
- **WebSocket (STOMP)** for live lineup
- **Auth:** Firebase Auth phone OTP → backend verifies Firebase ID token → issues app JWT with roles (Spring Security)
- **Push:** FCM, dispatched via **transactional outbox** + poller
- **Media / documents:** S3/Cloudflare R2 presigned uploads (backend never proxies file bytes). Verification documents in a **private** bucket/prefix, accessed only via short-lived presigned GET URLs for the owner and platform admins.
- **API docs:** springdoc-openapi (spec used to generate the Dart client)
- **Testing:** JUnit 5, Testcontainers (`postgis/postgis` image, Redis)
- **Observability:** Actuator + Micrometer
- **Local dev:** Docker Compose (Postgres+PostGIS, Redis)

### Mobile
- **Flutter** (latest stable), **Dart**
- **Riverpod** (state), **Dio** (HTTP), **go_router** (navigation)
- Generated API client from OpenAPI spec
- `flutter_map` + OSM tiles (maps), `cached_network_image`, FCM (`firebase_messaging`), Firebase Auth
- **Rive** for signature celebration animations; native Flutter implicit animations / `Hero` for UI motion
- Feature-first folder structure: `lib/features/<feature>/{data,domain,presentation}`, shared code in `lib/core/`

### Explicitly NOT using (don't suggest unless I ask)
Kafka, Kubernetes, Elasticsearch, microservices, GraphQL.

---

## 5. Key design decisions (already agreed — follow these)

1. **No slot double-booking — enforced by DB:** Postgres exclusion constraint on `(space_id, time_range)` with `tstzrange` + `&&`, filtered to active statuses. App code must not be the only guard.
2. **Nearby feed:** `geography(Point, 4326)` + GiST index; `ST_DWithin` + time window (e.g. next 7 days). **Keyset pagination** on `(start_time, id)` — no OFFSET. Only events at **verified** venues appear. All v1 events are public.
3. **Feed cache:** Redis, key = geohash cell (precision ~5) + date bucket, short TTL (30–60s). Invalidate/let expire on event changes.
4. **Capacity caps — atomic conditional UPDATE** (`... SET taken = taken + 1 WHERE taken < max`), check affected rows. No read-then-write. Applies to:
   - audience capacity (all formats with audience)
   - per-instrument **needs** in `CALL_FOR_MUSICIANS` (counted on **approval**, not on application)
   - total turns in `OPEN_MIC`
   - `OPEN_JAM` has **no instrument caps** (only an optional total performer cap).
5. **Event lifecycle = explicit state machine:** `REQUESTED → APPROVED | REJECTED → LIVE → COMPLETED`, plus `CANCELLED`. Venue self-created events start at `APPROVED`. Validate transitions centrally; keep an audit/history table.
6. **Participation rules per format — one strategy per format** in the participation module (e.g. a `JoinPolicy` per `format`), not `if/else` chains spread around:
   - `OPEN_JAM`: performer or audience → `CONFIRMED` instantly
   - `SHOWCASE`: audience only; performer join rejected
   - `CALL_FOR_MUSICIANS`: performer → `APPLIED` → host `APPROVED` / `REJECTED` (need-count consumed on approval); audience only if enabled
   - `OPEN_MIC`: performer → turn sign-up `CONFIRMED` with an ordered position; audience join as usual
   - Participation states: `APPLIED | CONFIRMED | REJECTED | CANCELLED | CHECKED_IN`
7. **BYOI / gear:** no venue gear inventory. `event.host_setup` (free text) + `event.amplified` (bool); `participation.bringing_own_instrument` (bool). **Sound-policy validation:** an amplified event cannot be created/approved in an `ACOUSTIC_ONLY` space, and event end time cannot exceed the space's sound curfew (local time in the venue's time zone).
8. **Hosting mode:** `OPEN` (hosts can request) | `SELF_ONLY` (only the venue creates events). Enforced in the event service, with tests.
9. **Notifications via transactional outbox:** outbox row written in the same transaction as the domain change; separate dispatcher sends to FCM with retries.
10. **Live lineup:** Redis holds queue order; changes published via Redis pub/sub → STOMP to clients (works across multiple backend instances). `OPEN_MIC` turn order seeds the lineup.
11. **DB access hygiene:** DTO projections for reads, avoid N+1, indexes justified by queries.
12. **Verification:**
   - `verification_status: PENDING → VERIFIED | REJECTED` on venue and host profile (with rejection reason).
   - Documents in a `verification_documents` table (owner type/id, doc type, storage key, status).
   - Venue: **FSSAI license number (14 digits) required**; lease / owner agreement upload optional. Host: verification optional (college/company ID).
   - Unverified venues can't publish slots or events; unverified hosts can still request (the venue decides), verified badge shown when verified.
   - **Never collect or store Aadhaar** (UIDAI rules / DPDP Act liability). Identity = phone OTP.
13. **Hosts as individuals or groups:** `host_profile` with `type: INDIVIDUAL | GROUP`, optional members (linked users), Instagram handle, `media_links` (URLs only).
14. **Host questions:** up to 3 per event (`event_questions`); answers per participation (`participation_answers`); visible to host and venue admin only.
15. **Fees are info only:** `performer_fee`, `audience_fee`, `perks` (text). No payment logic.
16. **Ratings:** `reviews` (event, reviewer, target type/id, rating 1–5, comment). Only after `COMPLETED`, only by checked-in participants / the venue / the host, one per reviewer-target-event (unique constraint).
17. **Reports (complaint centre):** two-way, tied to an event. `reports` status `OPEN → UNDER_REVIEW → ACTIONED | DISMISSED`. **No automatic blocking** — platform admin decides; actions recorded. `account_status: ACTIVE | SUSPENDED`; suspended accounts can't create/join events.
18. **Venue analytics** from app data only: RSVPs, check-ins, attendance rate, repeat attendees, per-event and weekly/monthly trends.

---

## 6. Code standards

**Backend**
- Layering per module: `controller → service → repository`; DTOs at the API boundary, never expose entities.
- Inside each module, one sub-package per layer: `controller/`, `service/`, `repository/`, `model/` (entities, enums), `dto/`, `exception/`, `config/`, plus focused ones where needed (e.g. `auth/firebase/`). Keep classes package-private unless another sub-package needs them.
- Validation with Jakarta Bean Validation; consistent error responses via `@RestControllerAdvice` (problem-details style).
- Constructor injection only. Immutable DTOs (records).
- Authorization checks (role + ownership) in the service layer, with tests.
- Meaningful tests for anything with logic, concurrency, or constraints (concurrent joins/approvals, overlapping slots, format rules, sound-policy and hosting-mode rules).
- No premature abstraction (no generic base services / repositories unless clearly needed). The per-format join policy is the one deliberate strategy pattern.

**Mobile**
- Keep widgets small; use `const` constructors wherever possible.
- `ListView.builder` for lists; scope Riverpod watches narrowly (`select`) to avoid unnecessary rebuilds.
- No business logic in widgets — it goes in providers/notifiers.
- Handle loading / error / empty states explicitly on every screen.
- Respect reduced-motion settings for all animations.

**General**
- Clear names over comments; comment only the non-obvious *why*.
- Config via env vars / profiles; no secrets committed.

---

## 7. Repo layout & dev setup

**Monorepo** (single git repo). Claude Code always runs from the **repo root** so it has full context of both backend and mobile.

```
jam-app/
├── CLAUDE.md          ← this file; Claude Code runs from root
├── .gitignore         ← combined Java/Maven + Flutter/Dart ignores
├── backend/           ← Spring Boot app (Maven) — I open this folder in IntelliJ IDEA
├── mobile/            ← Flutter app — I open this folder in VS Code (Flutter + Dart extensions)
├── infra/             ← docker-compose.yml, local setup scripts
└── docs/              ← PROGRESS.md, DECISIONS.md, api/openapi.yaml
```

Rules:
- Each step's plan must say which side(s) it touches (`backend/`, `mobile/`, `infra/`, `docs/`). Stay inside those folders.
- `backend/` and `mobile/` must each be independently openable/buildable as their own project (no cross-folder build dependencies, no root-level build file).
- **API contract flow:** backend is the source of truth → springdoc generates the OpenAPI spec (committed at `docs/api/openapi.yaml`, refreshed by the backend test suite) → Dart client generated into `mobile/` from that spec. When an API changes, update the spec and regenerate the client in the same step, and say so.
- Run/test commands you give me must specify the working directory (e.g. `cd backend && ./mvnw test`).
- Commits: one step = one commit, normal descriptive message prefixed by the area tag(s) it touches: `[infra] - <message>`, `[backend] - <message>`, `[mobile] - <message>`; combine tags when a step spans areas, e.g. `[backend][mobile] - <message>`. Root files and `docs/` go in the same step commit. No `Co-Authored-By` or other AI attribution trailers in commit messages.
- CI (later, not now): path-filtered — backend jobs on `backend/**`, mobile jobs on `mobile/**`.

---

## 8. Planned build order (one step at a time, each may be split further)

Steps already completed are recorded in `docs/PROGRESS.md` — that file is the source of truth for where we are.

1. Monorepo skeleton + Docker Compose (Postgres/PostGIS, Redis) + Spring Boot boot-up + Flyway baseline + health check
2. Auth: Firebase token verification → app JWT + roles; springdoc + committed OpenAPI spec
3. Venue & space CRUD (location, FSSAI number, capacity, hosting mode, sound policy + curfew, house rules, `verification_status`)
4. Host profiles (individual/group, members, Instagram, media links)
5. Verification + platform admin: document upload (presigned, private), admin review queue, approve/reject, `PLATFORM_ADMIN` role
6. Events + slots: event creation in 4 formats (details, tags, host setup, amplified, fees info, questions), host slot request + venue approval state machine, venue self-created events, exclusion constraint, hosting-mode + sound-policy validation, concurrency tests
7. Nearby feed (PostGIS, keyset pagination, verified venues only, filters incl. format) → then Redis cache
8. Participation: per-format join policies, atomic caps, Call-for-Musicians apply/approve, Open-Mic turn sign-up, host questions answers + tests
9. Flutter: project setup, auth, nearby feed (list → map)
10. Flutter: event detail, join flows per format, host profile + event request, venue onboarding + approvals
11. Notifications: outbox + FCM + reminders
12. Live lineup + check-in: Redis queue + WebSocket + Flutter live screen
13. Ratings + reports (complaint centre) + admin moderation + suspension enforcement (backend + Flutter)
14. Venue analytics (backend aggregates + Flutter dashboard)
15. Load test feed (k6), measure p95, document results

Don't start any step until I say so. When I say "next", propose the plan for the next step (section 3, point 1).