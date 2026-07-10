# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & run

Three local services. Run in this order.

### Database
```bash
docker compose up -d                 # postgres on host port 5433 (5432 taken by another container on this machine)
```

### Backend  (http://localhost:8081)
```bash
cd backend
# Build requires JDK 21. If JAVA_HOME points at JDK 23, export JAVA_HOME to a JDK 21 first.
./mvnw spring-boot:run               # Windows: mvnw.cmd spring-boot:run

# On this machine, ambient DB_URL/DB_USER/DB_PASSWORD env vars point at a Neon DB and override
# application.yml. Override them, plus port 8080 is taken so use 8081:
DB_URL=jdbc:postgresql://localhost:5433/ims DB_USER=ims DB_PASSWORD=ims SERVER_PORT=8081 ./mvnw spring-boot:run
# PowerShell:
#   $env:DB_URL='jdbc:postgresql://localhost:5433/ims'
#   $env:DB_USER='ims'; $env:DB_PASSWORD='ims'; $env:SERVER_PORT='8081'
#   ./mvnw spring-boot:run
```

Other Maven targets: `./mvnw clean package` (jar), `./mvnw test` (unit tests).
Swagger UI: http://localhost:8081/swagger-ui
Seeded logins: `super@ims.local` / `admin@demo.local`, both `Admin12345`.

### Frontend  (http://localhost:3001)
```bash
cd frontend
npm install
NEXT_PUBLIC_API_BASE=http://localhost:8081/api npm run dev
```
Port 3001 is hard-coded in `package.json` (`npm run dev = next dev -p 3001`) because 3000 is taken.
Backend CORS allowlist already covers 3000 and 3001 (`ims.cors.allowed-origins`).

## Architecture

Multi-tenant SaaS. **Shared DB, shared schema, `institute_id` on every tenant-owned row.** Isolation is enforced in two places — do not break either.

### Tenancy contract
- `com.ims.tenant.TenantAwareEntity` — `MappedSuperclass` for any tenant-owned entity. Carries `institute_id` and defines a Hibernate `@FilterDef("tenantFilter", parameter="tenantId")`. Subclasses must add `@Filter(name = "tenantFilter", condition = "institute_id = :tenantId")` for row-level scope.
- `com.ims.tenant.TenantContext` — `ThreadLocal` holding the current request's `instituteId` and a super-admin flag. Populated by `JwtAuthFilter`, cleared at end of request.
- `com.ims.tenant.TenantEntityListener` — `@PrePersist` stamps `institute_id` from `TenantContext` when not already set. Never trusts a client-supplied tenant on writes.
- `com.ims.tenant.TenantGuard.owned(entity)` — Hibernate `@Filter` is NOT applied to primary-key loads (`EntityManager.find`, `JpaRepository.findById`). Call `TenantGuard.owned(...)` on any tenant-owned entity fetched by id so a cross-tenant row returns 404. `SUPER_ADMIN` bypasses.
- `com.ims.tenant.TenantFilterAspect` — AOP advice that enables the `tenantFilter` on the active Hibernate session for non-super-admin requests. Requires a transaction to be open (the `PersistenceConfig` pins `@EnableTransactionManagement` to `Ordered.HIGHEST_PRECEDENCE` so the transaction wraps the aspect).

### Soft delete
- `com.ims.common.BaseEntity` — every entity extends this. Has `@SoftDelete(columnName = "deleted")` so every `delete()` becomes `UPDATE ... SET deleted = true` and reads are filtered by `deleted = false`. Records are archived for the life of the system.
- V7 migration adds partial unique indexes (`WHERE deleted = false`) so archived rows don't block re-using natural keys.

### Auth
- JWT access + refresh, issued by `com.ims.auth.AuthController` (`POST /api/auth/login`, `POST /api/auth/refresh`). Stateless. `SecurityConfig` is stateless, CSRF off, CORS allowlist driven by `ims.cors.allowed-origins`. The `/api/files/**` GETs are `permitAll()` (unguessable UUIDs).
- Roles: `SUPER_ADMIN / INSTITUTE_ADMIN / TEACHER / STUDENT / GUARDIAN` (`com.ims.auth.Role`). Method-level `@PreAuthorize` on controllers is the norm — `INSTITUTE_ADMIN` is tenant-scoped, `SUPER_ADMIN` is the platform operator.
- `com.ims.auth.User` is the principal. Not tenant-filtered (login resolves users by globally unique email before any tenant context exists). `instituteId` is null for `SUPER_ADMIN`, otherwise equals the user's tenant. `profileId` optionally links to a Student/Teacher/Guardian row.

### Backend package layout (`com.ims.*`)
- `auth/` — JWT, User, login, refresh, user CRUD
- `tenant/` — tenancy primitives (see above)
- `common/` — `BaseEntity`, `NotFoundException`, `BadRequestException`, `ApiError`, `PageResponse`, `GlobalExceptionHandler` (404/400/401/403/409/validation)
- `institute/` — tenant root entity, lifecycle
- `academic/` — academic year, grade/class, section (with class-teacher assignment)
- `people/` — Student, Teacher (with designation enum headmaster…PT), Guardian, student↔guardian links
- `admission/` — student → year/grade/section, status workflow
- `attendance/` — daily per-student marking (bulk upsert), list, summary %
- `exam/` — subjects, exam types, exams, marks (per subject upsert), computed marksheet/result (grade/GPA/pass-fail/class rank)
- `certificate/` — issue/list documents, PDF export via OpenPDF
- `fee/` — charges + payment ledger, `PENDING→PARTIAL→PAID`, waive, per-student summary; auto-journals to accounting on payment
- `routine/` — timetable slots: `CLASS` (weekly, section/day/time) and `EXAM` (dated, exam/date/time), per-kind validation
- `accounting/` — double-entry: financial years, chart of accounts (seeded per institute), manual + auto journal entries, ledger / trial balance / P&L / balance sheet
- `library/` `hostel/` `transport/` — facility modules (books/issues, rooms/allocations, vehicles/routes/assignments)
- `report/` — attendance report, exam result sheet, per-student marks history
- `file/` — binary file store (profile photos), `POST /api/files` upload, `GET /api/files/{uuid}` public download
- `staff/` — non-teaching employees (V9)
- `notice/` — notice board + in-app notifications (V10): notices with audience/grade/section targeting fan out one `Notification` row per matching user on create; `/api/notifications` is always self-scoped (current user)
- `portal/` — read-only role-scoped aggregates (`GET /api/portal/{student,guardian,teacher}`); scope always derived server-side from `User.profileId`, never from client params
- `config/` — `SecurityConfig`, `PersistenceConfig`, `DataSeeder` (idempotent dev seed — disable with `ims.seed.enabled=false`)

### Flyway migrations
`backend/src/main/resources/db/migration/V{1..10}__*.sql`. New schema changes get a new `V{N}__*.sql` — never edit an applied migration. Hibernate `ddl-auto: validate` will reject drift.

### Frontend structure
- App Router under `frontend/app/`. `app/dashboard/{students,teachers,guardians,academic,admissions,attendance,routine,fees,exams,certificates,reports,library,hostel,transport,users,settings,institutes}` — one folder per module. `app/login/`, `app/dashboard/layout.tsx` (uses `components/Nav.tsx`).
- `frontend/lib/api.ts` — `api<T>(path, options)` wrapper: adds Authorization + content-type, auto-refreshes on 401 once, throws `ApiError`. Also `uploadImage(file)`, `downloadFile(path, name)`, `fileUrl(relative)`. Tokens stored in `localStorage` as `ims_access` / `ims_refresh`.
- `frontend/lib/types.ts` — domain DTOs. `frontend/lib/hooks.ts` — query helpers.
- `frontend/components/DetailModal.tsx` — wide detail modal used across modules for the "View" action.
- `frontend/components/Nav.tsx` filters groups/items by `me.role` (`roles` arrays); `app/dashboard/page.tsx` branches by role to `components/portal/{StudentPortal,GuardianPortal,TeacherPortal}.tsx`; `components/NotificationBell.tsx` (unread badge, 30s poll) sits in the dashboard layout sidebar.
- Tailwind utility classes defined globally (`globals.css`) — `card`, `input`, `btn`, `btn-ghost`, `label`, `th`, `td`.

## Conventions
- **Backend:** Lombok `@Getter @Setter`, JPA repositories, soft delete via `BaseEntity`, tenant ownership via `TenantAwareEntity` + `TenantGuard.owned(...)` on every by-id fetch. Always use DTOs for controller boundaries; never serialize the entity. DTOs live next to the feature package in a `dto/` subpackage.
- **Frontend:** `"use client"` pages. Each module page typically has list (with search + inline create form), per-row edit/delete mutations, and a `DetailModal` for View. Mutations invalidate by `queryKey` and surface server `message` in a `text-red-600` line. Lists accept `?size=` (default 50) and an optional `?q=` for name search.
- **Tenant isolation acceptance gate:** a cross-tenant `GET /students/{A-id}` from a user in institute B must return 404. New by-id endpoints must call `TenantGuard.owned(entity)` to keep that property.
- `application.yml` env defaults assume the local Docker setup; ambient shell env can override them (see build & run).
