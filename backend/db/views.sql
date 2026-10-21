-- MedSentinel: Views

USE medsentinel;

-- V1: Open alerts view (for dashboard feed)
CREATE OR REPLACE VIEW v_open_alerts AS
SELECT
    a.alert_id,
    a.user_id,
    u.username,
    r.name AS role_name,
    a.rule_codes,
    a.score,
    a.severity,
    a.explanation,
    a.status,
    a.first_seen,
    a.last_seen,
    a.event_count,
    a.reviewer_note
FROM alerts a
JOIN users u ON a.user_id = u.user_id
JOIN roles r ON u.role_id = r.role_id
WHERE a.status IN ('OPEN', 'ACK', 'ESCALATED')
ORDER BY
    FIELD(a.severity, 'HIGH', 'MEDIUM', 'LOW'),
    a.last_seen DESC;

-- V2: User activity in the last 10 minutes (sliding window)
CREATE OR REPLACE VIEW v_user_activity_10min AS
SELECT
    al.user_id,
    u.username,
    r.name AS role_name,
    COUNT(*) AS total_actions,
    SUM(CASE WHEN al.outcome = 'ALLOWED' THEN 1 ELSE 0 END) AS allowed_count,
    SUM(CASE WHEN al.outcome = 'DENIED' THEN 1 ELSE 0 END) AS denied_count,
    SUM(CASE WHEN al.outcome = 'FAILED' THEN 1 ELSE 0 END) AS failed_count,
    MIN(al.ts) AS window_start,
    MAX(al.ts) AS window_end
FROM access_log al
JOIN users u ON al.user_id = u.user_id
JOIN roles r ON u.role_id = r.role_id
WHERE al.ts >= NOW(3) - INTERVAL 10 MINUTE
GROUP BY al.user_id, u.username, r.name;

-- V3: Alert evidence detail (joins alert → evidence → log)
CREATE OR REPLACE VIEW v_alert_evidence_detail AS
SELECT
    ae.alert_id,
    a.severity,
    a.explanation,
    al.log_id,
    al.ts,
    al.user_id,
    al.username_attempted,
    al.ip,
    al.action,
    al.resource_type,
    al.resource_id,
    al.patient_id,
    al.outcome,
    al.reason
FROM alert_evidence ae
JOIN alerts a ON ae.alert_id = a.alert_id
JOIN access_log al ON ae.log_id = al.log_id
ORDER BY ae.alert_id, al.ts;

-- V4: Daily access summary (for charts)
CREATE OR REPLACE VIEW v_daily_access_summary AS
SELECT
    DATE(ts) AS log_date,
    COUNT(*) AS total_events,
    SUM(CASE WHEN outcome = 'ALLOWED' THEN 1 ELSE 0 END) AS allowed,
    SUM(CASE WHEN outcome = 'DENIED' THEN 1 ELSE 0 END) AS denied,
    SUM(CASE WHEN outcome = 'FAILED' THEN 1 ELSE 0 END) AS failed,
    SUM(CASE WHEN action = 'LOGIN' AND outcome = 'FAILED' THEN 1 ELSE 0 END) AS failed_logins
FROM access_log
GROUP BY DATE(ts)
ORDER BY log_date DESC;

-- V5: Top risk users (users with highest open alert scores)
CREATE OR REPLACE VIEW v_top_risk_users AS
SELECT
    u.user_id,
    u.username,
    r.name AS role_name,
    COUNT(a.alert_id) AS open_alerts,
    COALESCE(MAX(a.score), 0) AS max_score,
    COALESCE(SUM(a.score), 0) AS total_score,
    MAX(a.severity) AS highest_severity
FROM users u
JOIN roles r ON u.role_id = r.role_id
LEFT JOIN alerts a ON u.user_id = a.user_id AND a.status IN ('OPEN', 'ACK', 'ESCALATED')
GROUP BY u.user_id, u.username, r.name
HAVING open_alerts > 0
ORDER BY total_score DESC;
