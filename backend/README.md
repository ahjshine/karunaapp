# 🛡️ MedSentinel (Karuna)
**Healthcare Security Operations Center (SOC) & Suspicious Clinical Access Detection System**

---

## 📌 Executive Summary

MedSentinel is an intelligent, explainable security monitoring backend and SOC dashboard built for hospital Electronic Health Record (EHR) systems. It provides real-time detection of insider threats, credential misuse, out-of-scope clinical snooping, and bulk record harvesting while protecting legitimate high-intensity clinical workflows (the **Busy-Clinician Guard**). Every access event is anchored into an append-only, SHA-256 cryptographically chained audit trail that detects any retroactive database tampering down to the exact corrupted record.

---

## 🚀 Key Architectural Highlights

- **Language & Runtime:** Java 17 LTS, Spring Boot 3.2.5, Maven 3.10.0.
- **Relational Persistence (3NF):** MySQL 8.0 schema with InnoDB engine, composite indexes, foreign key constraints, analytical views (`v_open_alerts`, `v_user_activity_10min`, `v_alert_evidence_detail`), and stored procedures (`sp_detect_failed_logins`, `sp_detect_bulk_access`, `sp_detect_out_of_scope`, `sp_verify_chain`).
- **Cryptographic Audit Trail:** Append-only SHA-256 hash chaining (`prev_hash` + canonical row representation) implemented in `LogChainService`, paired with database triggers (`trg_access_log_no_update`, `trg_access_log_no_delete`) that strictly prohibit retroactive modification or deletion.
- **Explainable Detection Engine:** Decoupled event-driven (`@EventListener`) detection rule pipeline:
  - **R1 / R1b:** Repeated failed logins (≥5 in 5 min, +30 pts) and immediate success after brute-force (+25 pts).
  - **R2:** Bulk record retrieval exceeding role baselines (+35 pts) equipped with the **Busy-Clinician Guard** (differentiating legitimate high-volume on-ward/care-team rounds from off-ward data mining).
  - **R3:** Out-of-scope unauthorized access attempts resulting in 403 HTTP denials (+40 pts).
  - **R4:** Off-hours access outside assigned shift times (+20 pts).
  - **R5:** Unassigned patient chart views (+15 pts).
- **Scoring & Alert Lifecycle:** Additive risk scoring mapped to severity (<30 log only, 30–49 Low, 50–69 Medium, ≥70 High), 10-minute sliding window correlation/deduplication, evidence linkage (`alert_evidence`), and reviewer workflows (Acknowledge, Escalate, False Positive).
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

---

## 🏗️ Getting Started & Running

### Prerequisites
- **Java 17+** (JDK 17 LTS recommended)
- **Maven 3.8+**
- **MySQL 8.0+** (or run test profile using in-memory H2)

### 1. Database Setup (MySQL 8)
Log into your MySQL instance and execute the schema scripts in order:

```bash
mysql -u root -p < db/schema.sql
mysql -u root -p < db/triggers.sql
mysql -u root -p < db/procedures.sql
mysql -u root -p < db/views.sql
mysql -u root -p < db/seed.sql
```

Ensure your credentials in `src/main/resources/application.yml` match your local MySQL server:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/medsentinel?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: your_password
```

### 2. Build and Test
Run the comprehensive test suite (38 unit, mock, and integration tests):
```bash
mvn clean test
```

Package the executable Spring Boot JAR:
```bash
mvn package
```

### 3. Launch the Application
Run the packaged JAR:
```bash
java -jar target/medsentinel-1.0.0.jar
```
Or start directly with Maven:
```bash
mvn spring-boot:run
```

Once started, open your browser at:
👉 **`http://localhost:8080`**

---

## 🎬 3-Minute Demo Walkthrough

### Step 1: Normal Nurse Shift (No False Positives)
1. Log in as `nurse.priya` (`password123`).
2. Navigate to **Patient Records**. View charts for Cardiology patients.
3. Access is granted; no alerts are generated.

### Step 2: Busy Clinician Burst (AC4 / PRD Goal G4)
1. Log in as `admin` (`password123`) and open **Scenario Simulator** (`/simulator`).
2. Click **Run Scenario: Busy clinician burst**.
3. Nurse Priya rapidly opens 40 charts on her assigned ward.
4. Open the **Security Dashboard** (`/dashboard`): **0 alerts created**. The baseline differentiates high-volume on-ward rounds from anomalous data harvesting.

### Step 3: Brute-Force Login & Out-of-Scope Bulk Pull
1. In the **Simulator**, click **Run Scenario: Brute-force login** (6 failed logins followed by success).
2. Click **Run Scenario: Billing clerk bulk pull** (Clerk Ravi attempts clinical access, then retrieves 38 billing records).
3. Return to **Security Dashboard** (`/dashboard`):
   - Notice the **High Severity Alert** created for `clerk.ravi` (Score: 75, Rules: `R3,R2`).
   - Notice the **Low/Medium Severity Alert** created for `dr.asha` (Rules: `R1,R1b`).
4. Click on an alert to inspect the **Evidence Drilldown** (`/dashboard/alerts/{id}`). The exact timestamps, IP addresses, resource types, and denied outcomes are linked.
5. Click **Escalate** or **Acknowledge** to update the incident status.

### Step 4: Doctor Emergency Break-Glass
1. In the **Simulator**, click **Run Scenario: Break-glass use** (Dr. Asha requires urgent access to an unassigned Neurology patient).
2. The override unblocks clinical access while creating a **Medium Severity Review Alert** (Score: 50, Rule: `BREAK_GLASS`) requiring justification audit.

### Step 5: Cryptographic Chain Verification & Tamper Detection
1. In the **Simulator**, click **Run Scenario: Tamper log row**. This sets session variable `@allow_log_tamper = 1` and modifies a row's cryptographic hash in the database.
2. Navigate to **Security Dashboard -> Verify Chain Integrity** (`/dashboard/integrity`).
3. The cryptographic verification engine reports **TAMPER DETECTED**, highlighting the exact broken row ID and the hash mismatch.

---

## 🧪 Automated Test Suite Summary

The backend includes 38 automated test cases covering all PRD acceptance criteria (AC1–AC6):

- `MedSentinelIntegrationTest`: Full end-to-end integration tests verifying Spring Security, RBAC filters, alert sliding window deduplication, busy-clinician guard, break-glass workflow, and cryptographic hash-chain tamper detection against H2.
- `AccessGuardTest`: Unit tests for ward boundaries, role permissions, and care team assignments.
- `BulkAccessRuleTest`: Validates R2 threshold logic and busy-clinician on-ward vs. off-ward separation.
- `FailedLoginRuleTest`: Validates R1 (failed login burst) and R1b (success after failure) detection.
- `OutOfScopeRuleTest`: Validates R3 unauthorized 403 attempt detection.
- `OffHoursRuleTest`: Validates R4 shift window detection.
- `UnassignedPatientRuleTest`: Validates R5 unassigned patient record views.
- `AlertServiceTest`: Tests 10-minute sliding window alert aggregation, rule merging, and score accumulation.
- `BreakGlassServiceTest`: Tests minimum justification length validation and incident generation.
- `LogChainServiceTest`: Tests SHA-256 canonical row serialization and chain verification.
- `ScenarioRunnerTest`: Mocks and validates all simulator test scenarios.
- `AuthTest`: Tests BCrypt hash validation for demo credentials.
