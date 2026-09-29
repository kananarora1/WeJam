# CLAUDE.md — Jam App (working name)

This file is the standing context + working agreement for this repo. Read it fully at the start of every session and follow it strictly.

---

## 1. What we're building

A mobile app for **live jam nights at restaurants/cafés**, built as a **learning + portfolio project** (not a product yet — no growth, payments, or monetization work).

**Actors**
- **Venue admin** — restaurant/café; lists spaces (capacity, gear, photos, availability), approves/rejects jam requests.
- **Host** — proposes a jam at a venue slot; sets genre/vibe; manages performers.
- **User** — discovers nearby jams; joins as **performer** (with instrument) or **audience**.

**Core flow:** Venue publishes space/availability → Host requests slot → Venue approves → Event goes live → Users join (performer/audience) → Live lineup on event night → Completed.

**Future (NOT now, but don't design it out):** open mics, shayari nights, standup. Keep `Event.type` generic (`JAM | OPEN_MIC | SHAYARI | ...`).

### In scope (v1)
1. Auth with roles: `USER`, `HOST`, `VENUE_ADMIN`
2. Venue + space CRUD (capacity, gear list, photos, availability)
3. Slot request → approve/reject (state machine)
4. Nearby jams feed (map + list, filter by date/genre)
5. Join as performer (capped per instrument) or audience (capped by capacity)
6. Live lineup/queue on event night (realtime)
7. Push notifications (approval, reminder, "you're up next")

### Out of scope
Payments, chat, reviews, recommendations, web app, admin panel, shareable web links.

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
- **Java 21**, **Spring Boot 3.x** (virtual threads enabled)
- **Modular monolith** — packages by feature/module: `auth`, `venue`, `event`, `participation`, `lineup`, `notification`, `common`. No microservices.
- **PostgreSQL + PostGIS** (Spring Data JPA + Hibernate Spatial where useful; native SQL where clearer)
- **Flyway** for all schema changes (never `ddl-auto=update`)
- **Redis** — feed cache, live lineup queue, pub/sub fanout
- **WebSocket (STOMP)** for live lineup
- **Auth:** Firebase Auth phone OTP → backend verifies Firebase ID token → issues app JWT with roles (Spring Security)
- **Push:** FCM, dispatched via **transactional outbox** + poller
- **Media:** S3/Cloudflare R2 presigned uploads (backend never proxies file bytes)
- **API docs:** springdoc-openapi (spec used to generate the Dart client)
- **Testing:** JUnit 5, Testcontainers (`postgis/postgis` image, Redis)
- **Observability:** Actuator + Micrometer
- **Local dev:** Docker Compose (Postgres+PostGIS, Redis)

### Mobile
- **Flutter** (latest stable), **Dart**
- **Riverpod** (state), **Dio** (HTTP), **go_router** (navigation)
- Generated API client from OpenAPI spec
- `flutter_map` + OSM tiles (maps), `cached_network_image`, FCM (`firebase_messaging`), Firebase Auth
- Feature-first folder structure: `lib/features/<feature>/{data,domain,presentation}`, shared code in `lib/core/`

### Explicitly NOT using (don't suggest unless I ask)
Kafka, Kubernetes, Elasticsearch, microservices, GraphQL.

---

## 5. Key design decisions (already agreed — follow these)

1. **No slot double-booking — enforced by DB:** Postgres exclusion constraint on `(space_id, time_range)` with `tstzrange` + `&&`, filtered to active statuses. App code must not be the only guard.
2. **Nearby feed:** `geography(Point, 4326)` + GiST index; `ST_DWithin` + time window (e.g. next 7 days). **Keyset pagination** on `(start_time, id)` — no OFFSET.
3. **Feed cache:** Redis, key = geohash cell (precision ~5) + date bucket, short TTL (30–60s). Invalidate/let expire on event changes.
4. **Capacity / performer caps:** atomic conditional UPDATE (`... SET taken = taken + 1 WHERE taken < max`), check affected rows. No read-then-write.
5. **Event lifecycle = explicit state machine:** `REQUESTED → APPROVED | REJECTED → LIVE → COMPLETED`, plus `CANCELLED`. Validate transitions centrally; keep an audit/history table.
6. **Notifications via transactional outbox:** outbox row written in the same transaction as the domain change; separate dispatcher sends to FCM with retries.
7. **Live lineup:** Redis holds queue order; changes published via Redis pub/sub → STOMP to clients (works across multiple backend instances).
8. **DB access hygiene:** DTO projections for reads, avoid N+1, indexes justified by queries.

---

## 6. Code standards

**Backend**
- Layering per module: `controller → service → repository`; DTOs at the API boundary, never expose entities.
- Validation with Jakarta Bean Validation; consistent error responses via `@RestControllerAdvice` (problem-details style).
- Constructor injection only. Immutable DTOs (records).
- Meaningful tests for anything with logic, concurrency, or constraints (e.g. concurrent join attempts, overlapping slot requests).
- No premature abstraction (no generic base services / repositories unless clearly needed).

**Mobile**
- Keep widgets small; use `const` constructors wherever possible.
- `ListView.builder` for lists; scope Riverpod watches narrowly (`select`) to avoid unnecessary rebuilds.
- No business logic in widgets — it goes in providers/notifiers.
- Handle loading / error / empty states explicitly on every screen.

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
└── docs/              ← PROGRESS.md, DECISIONS.md, API notes
```

Rules:
- Each step's plan must say which side(s) it touches (`backend/`, `mobile/`, `infra/`). Stay inside those folders.
- `backend/` and `mobile/` must each be independently openable/buildable as their own project (no cross-folder build dependencies, no root-level build file).
- **API contract flow:** backend is the source of truth → springdoc generates the OpenAPI spec (committed at `docs/api/openapi.yaml`) → Dart client generated into `mobile/` from that spec. When an API changes, update the spec and regenerate the client in the same step, and say so.
- Run/test commands you give me must specify the working directory (e.g. `cd backend && ./mvnw test`).
- Commits: one step = one commit, normal descriptive message prefixed by the area tag(s) it touches: `[infra] - <message>`, `[backend] - <message>`, `[mobile] - <message>`; combine tags when a step spans areas, e.g. `[infra][backend] - <message>`. Root files and `docs/` go in the same step commit. No `Co-Authored-By` or other AI attribution trailers in commit messages.
- CI (later, not now): path-filtered — backend jobs on `backend/**`, mobile jobs on `mobile/**`.

---

## 8. Planned build order (one step at a time, each may be split further)

Backend and mobile are interleaved: each backend feature is followed by the mobile screens that consume it. Backend steps are numbered, mobile steps are `M<n>`. Build in this order:

1. Monorepo skeleton (folders, combined `.gitignore`, `docs/` stubs) + Docker Compose (Postgres/PostGIS, Redis) + Spring Boot (Maven) boot-up + Flyway baseline + health check
2. Auth, split into:
   - 2a. App JWT + Spring Security + users & roles (fake Firebase verifier)
   - 2b. Real Firebase ID token verification
   - 2c. springdoc + committed `docs/api/openapi.yaml`
- **M1.** Flutter: project setup + phone OTP login → token exchange → "me" screen
3. Venue & space CRUD (with location)
- **M2.** Flutter: venue admin screens
4. Slot request + approval state machine + exclusion constraint + concurrency tests
- **M3.** Flutter: host slot request + venue approvals
5. Nearby feed (PostGIS, keyset pagination) → then Redis cache
- **M4.** Flutter: nearby feed (list → map)
6. Join flow (performer/audience) with atomic caps + tests
- **M5.** Flutter: event detail + join
7. Notifications: outbox + FCM + reminders
8. Live lineup: Redis queue + WebSocket + Flutter live screen
9. Load test feed (k6), measure p95, document results

Don't start any step until I say so. When I say "next", propose the plan for the next step (section 3, point 1).