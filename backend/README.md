# 🛡️ MedSentinel (Karuna)
**Healthcare Security Operations Center (SOC) & Suspicious Clinical Access Detection System**

---

## 📌 Executive Summary

MedSentinel is a high-performance, lightweight security monitoring backend and SOC dashboard built for hospital Electronic Health Record (EHR) systems. It provides real-time detection of insider threats, credential misuse, out-of-scope clinical snooping, and bulk record harvesting while protecting legitimate high-intensity clinical workflows (the **Busy-Clinician Guard**). Every access event is anchored into an append-only, SHA-256 cryptographically chained audit trail persisted in MongoDB that detects retroactive database tampering down to the exact corrupted record.

---

## 🚀 Key Architectural Highlights

- **Simplified Pure Java Architecture:** Built using Java 21 LTS with zero Spring Boot bloat, starting up in under 100ms.
- **MongoDB Persistence:** Native MongoDB driver persistence storing JSON/BSON documents in collections (`access_logs`, `alerts`, `users`, `patients`, `records`, `wards`, `roles`, `role_baselines`, `break_glass_events`).
- **Cryptographic Audit Trail:** Append-only SHA-256 hash chaining (`prev_hash` + canonical row representation) implemented in `LogChainService`, enabling instant verification of audit trail integrity.
- **Explainable Detection Engine:** Decoupled detection rule pipeline:
  - **R1 / R1b:** Repeated failed logins (≥5 in 5 min, +30 pts) and immediate success after brute-force (+25 pts).
  - **R2:** Bulk record retrieval exceeding role baselines (+35 pts) equipped with the **Busy-Clinician Guard** (differentiating legitimate high-volume on-ward/care-team rounds from off-ward data mining).
  - **R3:** Out-of-scope unauthorized access attempts resulting in 403 HTTP denials (+40 pts).
  - **R4:** Off-hours access outside assigned shift times (+20 pts).
  - **R5:** Unassigned patient chart views (+15 pts).
- **Scoring & Alert Lifecycle:** Additive risk scoring mapped to severity (<30 log only, 30–49 Low, 50–69 Medium, ≥70 High), 10-minute sliding window correlation/deduplication, direct evidence linkage, and reviewer workflows (Acknowledge, Escalate, False Positive).
- **Break-Glass Emergency Access:** Doctor emergency override flow requiring clinical justification (≥10 characters), immediate unblocked clinical record access, and automated generation of a Medium review alert.
- **Built-in Scenario Simulator:** One-click automated traffic runner firing attack and normal clinical workflows, plus a deterministic generator seeding 5,000 realistic, hash-chained logs spread across 7 days.

---

## 👥 Demo User Accounts

All accounts are pre-configured with the password: `password123`

| Username | Role | Assigned Ward | Shift Hours | Description / Typical Workflow |
| :--- | :--- | :--- | :--- | :--- |
| `dr.asha` | `DOCTOR` | Cardiology | 08:00 – 20:00 | Attending physician. Has clinical, billing, and break-glass access. |
| `nurse.priya` | `NURSE` | Cardiology | 08:00 – 20:00 | Staff nurse. Accesses on-ward patients (busy rounds). |
| `clerk.ravi` | `BILLING` | *None* | 09:00 – 18:00 | Billing clerk. Restricted from viewing clinical notes. |
| `rec.neha` | `RECEPTIONIST`| *None* | 09:00 – 18:00 | Front desk. Restricted to patient demographics only. |
| `rev.meera` | `REVIEWER` | *None* | 24/7 SOC | Security analyst. Investigates alerts, evidence, and log integrity. |
| `admin` | `ADMIN` | *None* | 24/7 SOC | Security admin. Manages users, tunes baselines, runs simulator. |
| `dr.elena.vance@stjude-health.org` | `DOCTOR` | Cardiology | 08:00 – 20:00 | Frontend placeholder doctor account with clinical, export, and break-glass access. |
| `nurse.sarah.connor@stjude-health.org` | `NURSE` | Cardiology | 08:00 – 20:00 | Frontend placeholder nurse account with ward-scoped clinical access. |

---

## 🏗️ Getting Started & Running

### Prerequisites
- **Java 21 LTS**
- **MongoDB** running locally on port 27017 (or MongoDB Atlas / Docker)

### 1. Launch the Application
Build the executable JAR from source:

```bash
npm run backend:build
```

Then run it:

```bash
java -jar backend/target/medsentinel-1.0.0.jar
```
Or from the project root:
```bash
npm run backend:java
```

Run the backend unit tests with:

```bash
npm run backend:test
```

The Maven POM is the authoritative Java 21 build, test, and package definition. The repository includes the Maven Wrapper, so Maven does not need to be installed globally. The equivalent direct commands are:

```bash
backend/mvnw -f backend/pom.xml test
backend/mvnw -f backend/pom.xml package
```

The package step produces the executable shaded JAR at `backend/target/medsentinel-1.0.0.jar`.

For a reproducible MongoDB + backend environment:

```bash
cd backend
docker compose up --build
```

Once started, open your browser at:
👉 **`http://localhost:8080`**

---

## 🧪 Automated Test Coverage

The backend tests are framework-free JUnit 5 unit tests over the pure Java services and rules. They cover:

- explicit record and export permissions, including ward boundaries;
- BCrypt authentication and rejection of malformed password hashes;
- failed-login, bulk-access, out-of-scope, off-hours, and unassigned-patient detection;
- break-glass validation and alert creation;
- strict SHA-256 hash-chain serialization and tamper detection;
- role bootstrap document serialization, including `roleId`;
- password-hash-free public user response serialization;
- HTTP permission enforcement and safe session-cookie attributes; and
- alert scoring, merging, and simulator behavior.

No Spring test context, JDBC test database, or MongoDB instance is required for these unit tests.

---

## 🎬 3-Minute Demo Walkthrough

### Step 1: Normal Nurse Shift (No False Positives)
1. Run normal nurse shift via simulator (`POST /api/simulator/normal-nurse`).
2. Access is granted; 0 alerts generated.

### Step 2: Busy Clinician Burst
1. Trigger `/api/simulator/busy-clinician`.
2. Nurse Priya rapidly opens 40 charts on her assigned ward.
3. **0 alerts created**; the baseline differentiates high-volume on-ward rounds from anomalous data harvesting.

### Step 3: Brute-Force Login & Out-of-Scope Bulk Pull
1. Trigger `/api/simulator/brute-force` (6 failed logins followed by success).
2. Trigger `/api/simulator/billing-bulk` (Clerk Ravi attempts clinical access, then retrieves 38 billing records).
3. Check alerts feed:
   - High Severity Alert created for `clerk.ravi` (Score: 75, Rules: `R3,R2`).
   - Medium Severity Alert created for `dr.asha` (Score: 55, Rules: `R1,R1b`).

### Step 4: Cryptographic Chain Verification & Tamper Detection
1. Trigger `/api/simulator/tamper-log`. This alters a log row's audit data in MongoDB.
2. Open `/dashboard/integrity` or call `GET /api/integrity`.
3. The cryptographic verification engine reports **TAMPER DETECTED**, highlighting the exact broken log ID and hash mismatch.

---

## 🔌 React UI integration

The React dashboard is connected to the Java API through `src/lib/medsentinelApi.ts`. The Vite development server proxies `/api` to `http://localhost:8080`, and the backend persists users, permissions, patients, records, alerts, and audit logs in MongoDB. The stable API surface is:

- `POST /api/auth/login`
- `GET /api/stats`
- `GET /api/alerts` and `GET /api/alerts/{id}`
- `POST /api/alerts/{id}/action`
- `GET /api/logs` and `GET /api/integrity`
- `GET /api/patients` and `GET /api/patients/{id}`
- `POST /api/simulator/{scenario}`

Use `VITE_API_BASE_URL` for deployments and the Vite `/api` proxy for local development. The frontend uses the authenticated session permissions to show only usable workspaces and actions; the backend remains the final authorization boundary.
