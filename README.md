# 🛡️ Karuna — Clinical Access Intelligence & MedSentinel SOC

Full-stack clinical access monitoring, anomaly detection, and zero-trust perimeter security system.

---

## 🏛️ System Architecture

This project is a hybrid enterprise system comprising:

```
┌────────────────────────────────────────────────────────┐
│  Vite Frontend (Port 5173)                             │
│  - Karuna Editorial Landing Page (shadcn/ui + Tailwind) │
│  - Zero-Trust Login Gateway with 3D DNA Helix (Three.js)│
│  - Clinical EHR Workstation with 2D Anatomical BodyMap  │
│  - Live in-browser mirror of MedSentinel Detection     │
└──────────────────────────┬─────────────────────────────┘
                           │
┌──────────────────────────▼─────────────────────────────┐
│  Spring Boot Backend (Port 8080) (`/backend`)          │
│  - Java 21 LTS + Spring Boot 3.2.5 + Spring Security   │
│  - Append-only SHA-256 cryptographic log chaining      │
│  - 5 Explainable Detection Rules (R1, R1b, R2, R3, R4) │
│  - Busy-Clinician Guard (on-ward vs. off-ward bounds)  │
│  - Pre-compiled executable JAR (medsentinel-1.0.0.jar) │
│  - Relational persistence in MySQL 8.0 or in-memory H2 │
└────────────────────────────────────────────────────────┘
```

---

## 🚀 Running the Project

### 1. Start the React Frontend (Port 5173)
```powershell
npm run dev
```
Open **`http://localhost:5173`**:
- **Public Site**: `http://localhost:5173`
- **Zero-Trust Login Gateway**: `http://localhost:5173/#login`
- **Security Dashboard**: `http://localhost:5173/#dashboard`

### 2. Start the Java Spring Boot Backend (Port 8080)
The backend is located in `backend/` and pre-compiled:

```powershell
# Run with in-memory H2 database (Zero setup, starts in ~10 seconds)
npm run backend:java

# Or if you have local MySQL 8.0 configured:
npm run backend:mysql
```
Open **`http://localhost:8080`** to access the native Spring Boot server directly.

---

## 👥 Demo Accounts

| Role | Username / Email | Password | Scope Token |
| :--- | :--- | :--- | :--- |
| **Doctor / Attending Physician** | `dr.asha` *(or `dr.elena.vance@stjude-health.org`)* | `password123` *(or `CyberShield2026!`)* | `EHR-FULL` |
| **Ward Nurse (Cardiology)** | `nurse.priya` *(or `nurse.sarah.connor@stjude-health.org`)*| `password123` | `VITALS-RW` |
| **Billing Clerk** | `clerk.ravi` | `password123` | `BILLING-ONLY` |
| **Receptionist** | `rec.neha` | `password123` | `DEMOGRAPHICS` |
| **Security Reviewer** | `rev.meera` | `password123` | `SOC-TIER4` |
| **System Admin** | `admin` | `password123` | `ROOT-PERM` |

---

## 🔬 Java Detection Engine Pipeline (`/backend`)

- **R1 / R1b**: Failed login brute-force detection (≥5 failures in 5 min) and immediate success alert.
- **R2 (Busy-Clinician Guard)**: High-volume rounds on assigned wards (e.g. Nurse Priya opening 40 charts) are recognized as legitimate and suppressed, while off-ward or unauthorized bulk pulls generate a High severity alert.
- **R3**: 403 HTTP out-of-scope denials.
- **R4 / R5**: Off-hours access outside scheduled shifts & unassigned patient charts.
- **LogChainService**: Append-only SHA-256 cryptographic log chaining with retroactive database update/delete triggers.
