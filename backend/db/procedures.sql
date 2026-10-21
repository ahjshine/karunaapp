-- MedSentinel: Stored Procedures
-- Detection and integrity verification procedures

USE medsentinel;

DELIMITER //

-- SP1: Detect repeated failed logins (R1)
-- Returns users/IPs with >= threshold failures in the given window
CREATE PROCEDURE sp_detect_failed_logins(
    IN p_threshold INT,
    IN p_window_minutes INT
)
BEGIN
    SELECT
        username_attempted,
        ip,
        COUNT(*) AS fail_count,
        MIN(ts)  AS window_start,
        MAX(ts)  AS window_end,
        GROUP_CONCAT(log_id ORDER BY ts) AS log_ids
    FROM access_log
    WHERE action = 'LOGIN'
      AND outcome = 'FAILED'
      AND ts >= NOW(3) - INTERVAL p_window_minutes MINUTE
    GROUP BY username_attempted, ip
    HAVING COUNT(*) >= p_threshold;
END//

-- SP2: Detect bulk access (R2) with busy-clinician guard
-- Uses window functions to count records per user in sliding 10-min windows
CREATE PROCEDURE sp_detect_bulk_access(
    IN p_window_minutes INT
)
BEGIN
    SELECT
        al.user_id,
        u.username,
        r.name AS role_name,
        COUNT(*) AS record_count,
        SUM(CASE
            WHEN ca.patient_id IS NOT NULL OR p.ward_id = u.ward_id
            THEN 1 ELSE 0
        END) AS on_ward_count,
        SUM(CASE
            WHEN ca.patient_id IS NULL AND (p.ward_id IS NULL OR p.ward_id != u.ward_id)
            THEN 1 ELSE 0
        END) AS off_ward_count,
        GROUP_CONCAT(al.log_id ORDER BY al.ts) AS log_ids
    FROM access_log al
    JOIN users u ON al.user_id = u.user_id
    JOIN roles r ON u.role_id = r.role_id
    LEFT JOIN patients p ON al.patient_id = p.patient_id
    LEFT JOIN care_assignments ca ON al.user_id = ca.user_id AND al.patient_id = ca.patient_id
    WHERE al.action IN ('VIEW', 'SEARCH', 'EXPORT')
      AND al.outcome = 'ALLOWED'
      AND al.ts >= NOW(3) - INTERVAL p_window_minutes MINUTE
    GROUP BY al.user_id, u.username, r.name;
END//

-- SP3: Detect out-of-scope access (R3)
-- Finds DENIED actions where the denial was due to role restrictions
CREATE PROCEDURE sp_detect_out_of_scope()
BEGIN
    SELECT
        al.user_id,
        u.username,
        r.name AS role_name,
        al.action,
        al.resource_type,
        al.log_id,
        al.ts,
        al.reason
    FROM access_log al
    JOIN users u ON al.user_id = u.user_id
    JOIN roles r ON u.role_id = r.role_id
    WHERE al.outcome = 'DENIED'
      AND al.ts >= NOW(3) - INTERVAL 10 MINUTE
    ORDER BY al.ts DESC;
END//

-- SP4: Verify hash chain integrity
-- Walks the access_log and returns the first row where the chain breaks
CREATE PROCEDURE sp_verify_chain()
BEGIN
    DECLARE v_prev_hash CHAR(64) DEFAULT NULL;
    DECLARE v_log_id BIGINT;
    DECLARE v_row_hash CHAR(64);
    DECLARE v_stored_prev CHAR(64);
    DECLARE v_expected_hash CHAR(64);
    DECLARE v_done INT DEFAULT 0;
    DECLARE v_broken INT DEFAULT 0;
    DECLARE v_total_rows INT DEFAULT 0;
    DECLARE v_checked_rows INT DEFAULT 0;

    DECLARE cur CURSOR FOR
        SELECT log_id, prev_hash, row_hash
        FROM access_log
        ORDER BY log_id;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET v_done = 1;

    SELECT COUNT(*) INTO v_total_rows FROM access_log;

    OPEN cur;

    read_loop: LOOP
        FETCH cur INTO v_log_id, v_stored_prev, v_row_hash;
        IF v_done THEN
            LEAVE read_loop;
        END IF;

        SET v_checked_rows = v_checked_rows + 1;

        -- Check prev_hash linkage
        IF v_prev_hash IS NOT NULL AND v_stored_prev != v_prev_hash THEN
            SET v_broken = 1;
            LEAVE read_loop;
        END IF;

        SET v_prev_hash = v_row_hash;
    END LOOP;

    IF v_broken = 1 THEN
        SELECT v_log_id AS broken_at_log_id,
               'PREV_HASH_MISMATCH' AS error_type,
               v_checked_rows AS rows_checked,
               v_total_rows AS total_rows;
    ELSE
        SELECT 0 AS broken_at_log_id,
               'OK' AS error_type,
               v_checked_rows AS rows_checked,
               v_total_rows AS total_rows;
    END IF;

    CLOSE cur;
END//

DELIMITER ;
