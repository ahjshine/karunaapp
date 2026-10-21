package com.medsentinel.simulator;

import com.medsentinel.alert.AlertService;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

/**
 * Runs demo scenarios to generate realistic access log traffic.
 */
@Service
public class ScenarioRunner {

    private static final Logger log = LoggerFactory.getLogger(ScenarioRunner.class);
    private final AccessLogService accessLogService;
    private final AlertService alertService;
    private final UserRepository userRepo;
    private final PatientRepository patientRepo;
    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    public ScenarioRunner(AccessLogService accessLogService,
                          AlertService alertService,
                          UserRepository userRepo,
                          PatientRepository patientRepo,
                          JdbcTemplate jdbcTemplate) {
        this.accessLogService = accessLogService;
        this.alertService = alertService;
        this.userRepo = userRepo;
        this.patientRepo = patientRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Normal nurse shift — views own-ward charts, should NOT trigger alerts.
     */
    public String runNormalNurseShift() {
        User nurse = userRepo.findByUsername("nurse.priya")
            .orElseThrow(() -> new RuntimeException("nurse.priya not found"));
        List<Patient> wardPatients = patientRepo.findByWardId(nurse.getWardId());

        int count = 0;
        for (Patient p : wardPatients) {
            accessLogService.logAccess(new AccessLog()
                .userId(nurse.getUserId())
                .usernameAttempted(nurse.getUsername())
                .action("VIEW")
                .resourceType("CLINICAL")
                .patientId(p.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Normal clinical access"));
            count++;
        }

        log.info("Scenario: Normal nurse shift — {} chart views", count);
        return String.format("Normal nurse shift: %d charts viewed (own ward). Expected: no alerts.", count);
    }

    /**
     * Busy clinician burst — nurse opens 40 own-ward charts fast.
     * Should NOT trigger an alert (high on-ward baseline).
     */
    public String runBusyClinician() {
        User nurse = userRepo.findByUsername("nurse.priya")
            .orElseThrow(() -> new RuntimeException("nurse.priya not found"));
        List<Patient> wardPatients = patientRepo.findByWardId(nurse.getWardId());

        int count = 0;
        // Open up to 40 charts, cycling through ward patients
        for (int i = 0; i < 40 && !wardPatients.isEmpty(); i++) {
            Patient p = wardPatients.get(i % wardPatients.size());
            accessLogService.logAccess(new AccessLog()
                .userId(nurse.getUserId())
                .usernameAttempted(nurse.getUsername())
                .action("VIEW")
                .resourceType("CLINICAL")
                .patientId(p.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Busy clinician burst — legitimate"));
            count++;
        }

        log.info("Scenario: Busy clinician burst — {} chart views", count);
        return String.format("Busy clinician burst: %d own-ward charts in rapid succession. Expected: NO alerts (on-ward baseline = 50).", count);
    }

    /**
     * Brute-force login attack — 6 failed logins then 1 success.
     * Should trigger R1 + R1b alerts.
     */
    public String runBruteForceLogin() {
        String targetUser = "dr.asha";
        String attackerIp = "192.168.1." + (100 + random.nextInt(155));

        // 6 failed attempts
        for (int i = 0; i < 6; i++) {
            accessLogService.logAccess(new AccessLog()
                .usernameAttempted(targetUser)
                .ip(attackerIp)
                .action("LOGIN")
                .outcome(AccessLog.Outcome.FAILED)
                .reason("Bad credentials"));
        }

        // Then a successful login
        User doctor = userRepo.findByUsername(targetUser).orElse(null);
        accessLogService.logAccess(new AccessLog()
            .userId(doctor != null ? doctor.getUserId() : null)
            .usernameAttempted(targetUser)
            .ip(attackerIp)
            .action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Authentication successful after brute force"));

        log.info("Scenario: Brute-force login attack on {}", targetUser);
        return String.format("Brute-force login: 6 failed + 1 success for '%s' from IP %s. Expected: R1 (30pts) + R1b (25pts) = Medium alert.", targetUser, attackerIp);
    }

    /**
     * Billing clerk bulk pull — clerk.ravi pulls many clinical records.
     * Should trigger R2 (bulk) + R3 (out of scope) = High alert.
     */
    public String runBillingClerkBulkPull() {
        User clerk = userRepo.findByUsername("clerk.ravi")
            .orElseThrow(() -> new RuntimeException("clerk.ravi not found"));
        List<Patient> allPatients = patientRepo.findAll();

        int count = 0;
        // First, denied clinical access attempts (R3)
        for (int i = 0; i < 5; i++) {
            Patient p = allPatients.get(i % allPatients.size());
            accessLogService.logAccess(new AccessLog()
                .userId(clerk.getUserId())
                .usernameAttempted(clerk.getUsername())
                .action("VIEW")
                .resourceType("CLINICAL")
                .patientId(p.getPatientId())
                .outcome(AccessLog.Outcome.DENIED)
                .reason("Role BILLING lacks permission VIEW_CLINICAL"));
            count++;
        }

        // Then bulk billing record access (R2)
        for (int i = 0; i < 38; i++) {
            Patient p = allPatients.get(i % allPatients.size());
            accessLogService.logAccess(new AccessLog()
                .userId(clerk.getUserId())
                .usernameAttempted(clerk.getUsername())
                .action("VIEW")
                .resourceType("BILLING")
                .patientId(p.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Billing access granted"));
            count++;
        }

        log.info("Scenario: Billing clerk bulk pull — {} events", count);
        return String.format("Billing clerk bulk pull: %d total access events (5 clinical denied + 38 billing). Expected: R3 (40pts) + R2 (35pts) = High alert.", count);
    }

    /**
     * Receptionist reads clinical — should be denied and trigger R3.
     */
    public String runReceptionistReadsClinical() {
        User rec = userRepo.findByUsername("rec.neha")
            .orElseThrow(() -> new RuntimeException("rec.neha not found"));
        Patient patient = patientRepo.findAll().get(0);

        accessLogService.logAccess(new AccessLog()
            .userId(rec.getUserId())
            .usernameAttempted(rec.getUsername())
            .action("VIEW")
            .resourceType("CLINICAL")
            .patientId(patient.getPatientId())
            .outcome(AccessLog.Outcome.DENIED)
            .reason("Role RECEPTIONIST lacks permission VIEW_CLINICAL"));

        log.info("Scenario: Receptionist attempted clinical access");
        return String.format("Receptionist clinical access: denied for '%s'. Expected: R3 alert (40pts = Medium).", rec.getUsername());
    }

    /**
     * Break-glass use by doctor on off-ward patient.
     */
    public String runBreakGlass() {
        User doctor = userRepo.findByUsername("dr.asha")
            .orElseThrow(() -> new RuntimeException("dr.asha not found"));
        // Pick a patient NOT in the doctor's ward
        Patient offWardPatient = patientRepo.findAll().stream()
            .filter(p -> !p.getWardId().equals(doctor.getWardId()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No off-ward patient found"));

        AccessLog logEntry = accessLogService.logAccess(new AccessLog()
            .userId(doctor.getUserId())
            .usernameAttempted(doctor.getUsername())
            .action("BREAK_GLASS")
            .resourceType("CLINICAL")
            .patientId(offWardPatient.getPatientId())
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Break-glass: Emergency consult, patient in distress"));

        // Break-glass is always a Medium reviewer item, even when the access itself is valid.
        alertService.createOrMergeAlert(doctor.getUserId(), "BREAK_GLASS", 50, "MEDIUM",
            String.format("Break-glass access by %s on off-ward patient '%s'. Justification: Emergency consult, patient in distress",
                doctor.getUsername(), offWardPatient.getName()),
            List.of(logEntry.getLogId()));

        log.info("Scenario: Break-glass by {} on patient {}", doctor.getUsername(), offWardPatient.getName());
        return String.format("Break-glass: %s accessed off-ward patient '%s'. Expected: Medium review alert.", doctor.getUsername(), offWardPatient.getName());
    }

    /**
     * Tamper a log row — directly updates a row to demonstrate integrity break.
     */
    public String tamperLogRow() {
        try {
            // Find a row to tamper
            Long logId = jdbcTemplate.queryForObject(
                "SELECT log_id FROM access_log WHERE row_hash IS NOT NULL ORDER BY log_id LIMIT 1 OFFSET 5",
                Long.class);

            if (logId == null) {
                return "No log rows available to tamper. Run some scenarios first.";
            }

            // This session variable is honored only by the append-only trigger for this
            // controlled demo. Normal app sessions cannot update audit rows.
            jdbcTemplate.execute("SET @allow_log_tamper = 1");
            try {
                jdbcTemplate.update(
                    "UPDATE access_log SET reason = 'TAMPERED_FOR_DEMO' WHERE log_id = ?", logId);
            } finally {
                jdbcTemplate.execute("SET @allow_log_tamper = NULL");
            }

            log.warn("Scenario: Tampered log row {}", logId);
            return String.format("Tampered log row %d. Run 'Verify Integrity' to detect the break.", logId);
        } catch (Exception e) {
            return "Could not tamper log: " + e.getMessage();
        }
    }
}
