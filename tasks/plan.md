# Implementation Plan: Secure Backend and UI Integration

## Overview

Make the existing Java 21/MongoDB backend the real source of data for the React dashboard while preserving the current UI styling. The backend will use simple Java classes and explicit composition, with cookie-backed sessions, route authorization, safe DTOs, deterministic audit logging, and a stable API adapter in the frontend.

## Architecture Decisions

- Keep Java 21, `HttpServer`, MongoDB, Gson, and jBCrypt; do not reintroduce Spring Boot.
- Use an opaque, in-memory session token in an `HttpOnly` cookie for this single-process demo backend. The token is never returned in JSON or stored in `localStorage`.
- Keep the existing API paths and Mongo collection names. Adapt field names in one frontend API client.
- Pass the authenticated `AppUserDetails` explicitly into patient operations; remove the implicit doctor fallback.
- Keep simulator/tamper routes available only to `ADMIN` users.
- Use allowlisted response DTOs for users and login results so password hashes never cross the HTTP boundary.

## Task List

### Phase 1: Security and API foundation

- [x] Add session store, request principal resolution, cookie parsing, security headers, and structured API errors.
- [x] Enforce authentication and permissions for protected routes.
- [x] Remove the doctor fallback and fix explicit permission checks.
- [x] Protect admin/simulator routes and exclude password hashes from responses.

### Phase 2: Persistence and test foundation

- [x] Fix role IDs in bootstrap data, strict append-only log writes, and seed-chain validation.
- [x] Replace stale Spring test assumptions with framework-free tests and add HTTP contract coverage.
- [x] Make Maven packaging/test commands authoritative and document prerequisites.

### Phase 3: React integration

- [x] Add one typed API adapter and Vite `/api` proxy.
- [x] Replace login, dashboard alerts/logs/integrity, reviewer actions, simulator, and clinical access calls with backend API calls.
- [x] Add loading/error/empty states without changing the current visual design.

### Checkpoint: Complete

- [x] Backend main sources compile.
- [x] Backend tests pass.
- [x] Frontend build and lint pass.
- [x] Login-to-dashboard-to-API flow is verified against a running backend and MongoDB.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Existing worktree contains unrelated UI edits | High | Preserve current files and change only integration points needed for the API boundary. |
| Maven or MongoDB unavailable locally | High | Keep a direct `javac` compile gate, report unavailable environment checks, and use Docker Compose when the daemon is available. |
| Backend response fields differ from UI models | Medium | Normalize data in `src/lib/medsentinelApi.ts` rather than spreading mappings through components. |
| Session state is process-local | Medium | Document it as a demo deployment constraint and keep the token opaque/httpOnly. |

## Open Questions

- None required to implement the requested local demo integration.
