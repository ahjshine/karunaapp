-- MedSentinel: Database Schema
-- MySQL 8 compatible

CREATE DATABASE IF NOT EXISTS medsentinel
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE medsentinel;

-- Roles
CREATE TABLE roles (
    role_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Permissions
CREATE TABLE permissions (
    perm_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    code      VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Role-Permission mapping
CREATE TABLE role_permissions (
    role_id   BIGINT NOT NULL,
    perm_id   BIGINT NOT NULL,
    PRIMARY KEY (role_id, perm_id),
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (perm_id) REFERENCES permissions(perm_id)
) ENGINE=InnoDB;

-- Wards
CREATE TABLE wards (
    ward_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name      VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- Users
CREATE TABLE users (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50) NOT NULL UNIQUE,
    pw_hash     VARCHAR(255) NOT NULL,
    role_id     BIGINT NOT NULL,
    ward_id     BIGINT NULL,
    shift_start TIME DEFAULT '08:00:00',
    shift_end   TIME DEFAULT '20:00:00',
    status      ENUM('ACTIVE', 'DISABLED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (role_id) REFERENCES roles(role_id),
    FOREIGN KEY (ward_id) REFERENCES wards(ward_id)
) ENGINE=InnoDB;

-- Patients
CREATE TABLE patients (
    patient_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    dob         DATE NOT NULL,
    ward_id     BIGINT NOT NULL,
    FOREIGN KEY (ward_id) REFERENCES wards(ward_id)
) ENGINE=InnoDB;

-- Care assignments (which users are assigned to which patients)
CREATE TABLE care_assignments (
    user_id     BIGINT NOT NULL,
    patient_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, patient_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
) ENGINE=InnoDB;

-- Patient records
CREATE TABLE records (
    record_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id  BIGINT NOT NULL,
    type        ENUM('CLINICAL', 'BILLING', 'DEMOGRAPHIC') NOT NULL,
    content     TEXT NOT NULL,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
) ENGINE=InnoDB;

-- Role baselines (detection thresholds per role)
CREATE TABLE role_baselines (
    baseline_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id                 BIGINT NOT NULL,
    scope                   ENUM('ON_WARD', 'OFF_WARD', 'CLINICAL', 'ANY') NOT NULL,
    max_records_per_10min   INT NOT NULL,
    multiplier              DECIMAL(3,1) NOT NULL DEFAULT 1.5,
    UNIQUE KEY uk_role_scope (role_id, scope),
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
) ENGINE=InnoDB;

-- Access log (append-only, hash-chained)
CREATE TABLE access_log (
    log_id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ts                  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    user_id             BIGINT NULL,
    username_attempted  VARCHAR(50) NOT NULL,
    ip                  VARCHAR(45) NOT NULL DEFAULT '127.0.0.1',
    action              VARCHAR(30) NOT NULL,
    resource_type       VARCHAR(30) NULL,
    resource_id         BIGINT NULL,
    patient_id          BIGINT NULL,
    outcome             ENUM('ALLOWED', 'DENIED', 'FAILED') NOT NULL,
    reason              VARCHAR(500) NULL,
    prev_hash           CHAR(64) NULL,
    row_hash            CHAR(64) NULL,
    INDEX idx_user_ts (user_id, ts),
    INDEX idx_outcome_ts (outcome, ts),
    INDEX idx_ip_ts (ip, ts),
    INDEX idx_patient_ts (patient_id, ts),
    INDEX idx_action_ts (action, ts)
) ENGINE=InnoDB;

-- Alerts
CREATE TABLE alerts (
    alert_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NULL,
    rule_codes  VARCHAR(100) NOT NULL,
    score       INT NOT NULL DEFAULT 0,
    severity    ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    explanation TEXT NOT NULL,
    status      ENUM('OPEN', 'ACK', 'ESCALATED', 'FALSE_POSITIVE') NOT NULL DEFAULT 'OPEN',
    reviewer_note VARCHAR(500) NULL,
    first_seen  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    last_seen   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    event_count INT NOT NULL DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_user_alert (user_id, status)
) ENGINE=InnoDB;

-- Alert evidence (links alerts to access log rows)
CREATE TABLE alert_evidence (
    alert_id    BIGINT NOT NULL,
    log_id      BIGINT NOT NULL,
    PRIMARY KEY (alert_id, log_id),
    FOREIGN KEY (alert_id) REFERENCES alerts(alert_id),
    FOREIGN KEY (log_id) REFERENCES access_log(log_id)
) ENGINE=InnoDB;

-- Break-glass events
CREATE TABLE break_glass_events (
    bg_id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    patient_id      BIGINT NOT NULL,
    justification   VARCHAR(500) NOT NULL,
    ts              DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    reviewed        BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id)
) ENGINE=InnoDB;
