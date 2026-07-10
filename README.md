# IMS — SaaS Institute Management System (MVP core)

Multi-tenant institute management. **Spring Boot** API (built with **Maven**) + **Next.js** frontend + **PostgreSQL**.
Tenancy model: shared DB, `institute_id` on every tenant-owned row, enforced by a Hibernate
filter (queries) plus a `TenantGuard` on primary-key loads.

## MVP scope built
- Auth: JWT (access+refresh), roles `SUPER_ADMIN / INSTITUTE_ADMIN / TEACHER / STUDENT / GUARDIAN`.
- User administration: full CRUD on users (create/list/read/update/delete), role assignment,
  enable/disable, admin password reset, self change-password — tenant-scoped and role-enforced
  (institute admins manage only their own tenant, cannot grant SUPER_ADMIN).
- Certificates export to **PDF** (`GET /api/certificates/{id}/pdf`).
- Institute (tenant root), created by super-admin together with its first admin.
- Academic year, grade/class, section (with class-teacher assignment).
- People: full CRUD for student, teacher (designation: headmaster … PT), and guardian
  (create/list/search/get/update/delete), plus student↔guardian links (relation type).
- **Soft delete everywhere**: deletes never remove data. Hibernate `@SoftDelete` (on `BaseEntity`)
  turns every delete into `UPDATE deleted = true` and filters `deleted = false` on all reads, so
  records are archived for the life of the system and stay queryable in the database.
- Admission: student → year/grade/section, status workflow.
- Exam/evaluation: subjects, exam types, exams, marks (per subject, upsert), and a computed
  **marksheet/result** (per-subject grade, totals, percentage, GPA, letter, pass/fail, class rank).
- Certificates: issue and list documents (marksheet/transfer/character/completion/…) per student.
- Attendance: daily per-student marking (bulk upsert), list by date/section, per-student summary %.
- Fees: fee charges + payment ledger; record payments (status PENDING→PARTIAL→PAID, overpay
  rejected), waive, per-student billed/paid/due summary.
- Routine: unified timetable slots — **CLASS** (weekly, by section/day/time) and **EXAM** (dated,
  by exam/date/time), with per-kind validation.
- Accounting (double-entry): financial years, chart of accounts (seeded per institute), balanced
  journal entries (manual + draft/post), **auto-journals** from fee payments (Dr Cash/Bank, Cr Fee
  Income, cash-basis), and reports — **ledger, trial balance, profit & loss, balance sheet**.
- Library: books (CRUD, copy tracking) + issue/return with fines.
- Hostel: hostels, rooms (occupancy), student allocation/vacate.
- Transport: vehicles, routes (fare/stops), student route assignments.
- Frontend: login + admin dashboard with CRUD for all of the above, marks entry + marksheet view,
  weekly class grid + exam schedule.

Later phases (not built): reexam/recheck, SaaS billing, notifications.

Grading scale (marksheet): pass ≥ 33%; A+ ≥80, A ≥70, A- ≥60, B ≥50, C ≥40, D ≥33, else F.

## Prerequisites
Java 21, Node 20+, Docker.

## 1. Database
```
docker compose up -d          # postgres
```
> Compose publishes Postgres on host port **5433** (5432 was already taken on this machine
> by another container). The app defaults to 5433.

## 2. Backend  (http://localhost:8081)
```
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```
Other Maven targets: `./mvnw clean package` (build jar), `./mvnw test` (tests).
> Build requires **JDK 21**. If your default `JAVA_HOME` points elsewhere (this machine defaults
> to JDK 23), set `JAVA_HOME` to a JDK 21 for Maven.
>
> Default server port is **8081** in these docs because **8080 was occupied** on this machine.
> Change with `SERVER_PORT`.
>
> ⚠️ This machine has ambient `DB_URL` / `DB_USER` / `DB_PASSWORD` env vars pointing at a Neon
> database — they override `application.yml`. When running locally, set them explicitly:
> ```
> DB_URL=jdbc:postgresql://localhost:5433/ims DB_USER=ims DB_PASSWORD=ims SERVER_PORT=8081 ./mvnw spring-boot:run
> ```
> (PowerShell: set `$env:DB_URL=...`, `$env:JAVA_HOME=...` etc. first.)

Flyway runs migrations on boot; a dev `DataSeeder` creates:
- Super admin — `super@ims.local` / `Admin12345`
- Demo institute `DEMO` + admin — `admin@demo.local` / `Admin12345`

Swagger UI: http://localhost:8081/swagger-ui

## 3. Frontend  (http://localhost:3001)
```
cd frontend
npm install
NEXT_PUBLIC_API_BASE=http://localhost:8081/api npm run dev
```
Log in as `admin@demo.local` / `Admin12345`. (Port 3001 because 3000 was taken;
the API's CORS allowlist includes both 3000 and 3001.)

## Verify tenant isolation
`scratchpad/smoke.ps1`-style flow: create two institutes A & B, add data under A, then confirm
B sees 0 rows and a cross-tenant `GET /students/{A-id}` returns **404**. This is the acceptance
gate for the shared-DB model.

## Layout
```
backend/   Spring Boot / Maven (com.ims: auth, tenant, common, institute, academic,
           people, admission, exam, certificate, attendance, fee, routine, accounting, config)
frontend/  Next.js App Router (app/, components/, lib/)
docker-compose.yml
```
