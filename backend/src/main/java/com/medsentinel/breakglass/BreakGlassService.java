package com.medsentinel.breakglass;

import com.medsentinel.alert.AlertService;
import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.patient.PatientRecord;
import com.medsentinel.patient.RecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Handles break-glass emergency access for doctors.
 * Grants access, logs it, and creates a review alert.
 */
@Service
public class BreakGlassService {

    private static final Logger log = LoggerFactory.getLogger(BreakGlassService.class);

    private final BreakGlassRepository breakGlassRepo;
    private final PatientRepository patientRepo;
    private final RecordRepository recordRepo;
    private final AccessLogService accessLogService;
    private final AlertService alertService;

    public BreakGlassService(BreakGlassRepository breakGlassRepo,
                              PatientRepository patientRepo,
                              RecordRepository recordRepo,
                              AccessLogService accessLogService,
                              AlertService alertService) {
        this.breakGlassRepo = breakGlassRepo;
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.accessLogService = accessLogService;
        this.alertService = alertService;
    }

    /**
     * Execute break-glass access. Only doctors can use this.
     * Requires justification >= 10 chars.
     */
    @Transactional
    public BreakGlassResult executeBreakGlass(AppUserDetails user, Long patientId,
                                               String justification) {
        // Validate
        if (!"DOCTOR".equals(user.getRoleName())) {
            throw new IllegalStateException("Only doctors can use break-glass access");
        }
        if (justification == null || justification.length() < 10) {
            throw new IllegalArgumentException("Justification must be at least 10 characters");
        }

        Patient patient = patientRepo.findById(patientId)
            .orElseThrow(() -> new IllegalArgumentException("Patient not found: " + patientId));

        // Record the break-glass event
        BreakGlassEvent event = new BreakGlassEvent();
        event.setUserId(user.getUserId());
        event.setPatientId(patientId);
        event.setJustification(justification);
        breakGlassRepo.save(event);

        // Log the access
        AccessLog logEntry = accessLogService.logAccess(new AccessLog()
            .userId(user.getUserId())
            .usernameAttempted(user.getUsername())
            .action("BREAK_GLASS")
            .resourceType("CLINICAL")
            .patientId(patientId)
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Break-glass: " + justification));

        // Create a Medium alert for reviewer follow-up
        alertService.createOrMergeAlert(
            user.getUserId(), "BREAK_GLASS", 50, "MEDIUM",
            String.format("Break-glass access by %s on patient '%s'. Justification: %s",
                user.getUsername(), patient.getName(), justification),
            List.of(logEntry.getLogId())
        );

        log.warn("Break-glass: user={}, patient={}, justification={}",
            user.getUsername(), patientId, justification);

        // Fetch all records for the patient
        List<PatientRecord> records = recordRepo.findByPatientId(patientId);

        return new BreakGlassResult(event, records);
    }

    public List<BreakGlassEvent> getUnreviewedEvents() {
        return breakGlassRepo.findByReviewedFalseOrderByTsDesc();
    }

    @Transactional
    public void markReviewed(Long bgId) {
        BreakGlassEvent event = breakGlassRepo.findById(bgId)
            .orElseThrow(() -> new IllegalArgumentException("Break-glass event not found"));
        event.setReviewed(true);
        breakGlassRepo.save(event);
    }

    public record BreakGlassResult(BreakGlassEvent event, List<PatientRecord> records) {}
}
