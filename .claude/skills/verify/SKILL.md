---
name: verify
description: Build, launch, and drive the IMS app (Spring Boot backend + Next.js frontend) to verify changes end-to-end.
---

# Verifying IMS changes

## Launch

1. DB: `docker compose up -d` (postgres on host port **5433**; container often already running).
2. Backend (from `backend/`, needs JDK 21 — ambient JAVA_HOME points at JDK 23):
   ```bash
   JAVA_HOME="C:/Program Files/Java/jdk-21" DB_URL=jdbc:postgresql://localhost:5433/ims \
     DB_USER=ims DB_PASSWORD=ims SERVER_PORT=8081 ./mvnw.cmd spring-boot:run
   ```
   Run in background; ~35s to start. Watch the log for Flyway applying new migrations
   ("now at version vN") — Hibernate `ddl-auto: validate` fails fast on schema drift.
3. Frontend: the user's `next dev -p 3001` is usually **already running** (EADDRINUSE if you
   try to start another) and points at 8081; hot reload picks up new pages. Only start your
   own (`NEXT_PUBLIC_API_BASE=http://localhost:8081/api npm run dev`) if 3001 is free.

## Drive

- API: login `POST /api/auth/login` with `admin@demo.local` / `Admin12345` (institute admin)
  or `super@ims.local` / `Admin12345` (super admin), then Bearer token.
- UI: Playwright works headless. `npm i playwright` in the scratchpad +
  `npx playwright install chromium` (browsers cache under `%LOCALAPPDATA%/ms-playwright`).
  Login form: fill `input[type=email]` / `input[type=password]`, click
  `button:has-text("Sign in")` (no `type=submit` attribute), wait for `**/dashboard**`.

## Gotchas

- `/actuator/health` returns 403 (not exposed) — use `POST /api/auth/login` status as the
  readiness check instead.
- Unauthenticated API requests return **403**, not 401 (no auth entry point configured).
- `GET /api/grades` and `GET /api/sections` return plain JSON **arrays**; most other list
  endpoints return a `PageResponse` (`{content: [...]}`). Check the controller before typing
  the frontend query.
- Tenant-isolation gate: a by-id GET for another institute's row must 404
  (`TenantGuard.owned`). Probe it when adding by-id endpoints.
