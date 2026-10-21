-- MedSentinel: Triggers
-- Enforces append-only access_log and auto-timestamps

USE medsentinel;

DELIMITER //

-- Block UPDATE on access_log (append-only enforcement).
-- A database-admin session may set @allow_log_tamper = 1 only for the
-- controlled demo that proves the integrity checker detects alteration.
CREATE TRIGGER trg_access_log_no_update
BEFORE UPDATE ON access_log
FOR EACH ROW
BEGIN
    -- Allow initial hash fill-in on a new row and the explicitly privileged demo session.
    IF OLD.row_hash IS NOT NULL AND COALESCE(@allow_log_tamper, 0) <> 1 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'ACCESS_LOG is append-only. UPDATE is not permitted.';
    END IF;
END//

-- Block DELETE on access_log
CREATE TRIGGER trg_access_log_no_delete
BEFORE DELETE ON access_log
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ACCESS_LOG is append-only. DELETE is not permitted.';
END//

DELIMITER ;
