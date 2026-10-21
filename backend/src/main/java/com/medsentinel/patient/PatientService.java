package com.medsentinel.patient;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.rbac.AccessGuard;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Single entry point for patient data access.
 * Enforces RBAC and logs every attempt.
 */
@Service
public class PatientService {

    private final PatientRepository patientRepo;
    private final RecordRepository recordRepo;
    private final AccessGuard accessGuard;
    private final AccessLogService accessLogService;

    public PatientService(PatientRepository patientRepo, RecordRepository recordRepo,
                          AccessGuard accessGuard, AccessLogService accessLogService) {
        this.patientRepo = patientRepo;
        this.recordRepo = recordRepo;
        this.accessGuard = accessGuard;
        this.accessLogService = accessLogService;
    }

    public List<Patient> listPatients() {
        return patientRepo.findAll();
    }

    public Patient getPatient(Long patientId) {
        return patientRepo.findById(patientId).orElse(null);
    }

    /**
     * Fetch records for a patient, enforcing RBAC.
     * Returns the records if allowed, throws AccessDeniedException if not.
     */
    public List<PatientRecord> getRecords(Long patientId, PatientRecord.RecordType type) {
        AppUserDetails user = getCurrentUser();
        String ip = "127.0.0.1"; // In a real app, extracted from request

        // Check permission
        String denialReason = accessGuard.checkAccess(user, type, patientId);

        if (denialReason != null) {
            // Log denied access
            accessLogService.logAccess(new AccessLog()
                .userId(user.getUserId())
                .usernameAttempted(user.getUsername())
                .ip(ip)
                .action("VIEW")
                .resourceType(type.name())
                .patientId(patientId)
                .outcome(AccessLog.Outcome.DENIED)
                .reason(denialReason));

            throw new AccessDeniedException(denialReason);
        }

        // Fetch records
        List<PatientRecord> records = recordRepo.findByPatientIdAndType(patientId, type);

        // Log allowed access
        for (PatientRecord record : records) {
            accessLogService.logAccess(new AccessLog()
                .userId(user.getUserId())
                .usernameAttempted(user.getUsername())
                .ip(ip)
                .action("VIEW")
                .resourceType(type.name())
                .resourceId(record.getRecordId())
                .patientId(patientId)
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Access granted"));
        }

        return records;
    }

    /**
     * View all record types the user is allowed to see for a patient.
     */
    public PatientRecordBundle getAccessibleRecords(Long patientId) {
        AppUserDetails user = getCurrentUser();
        PatientRecordBundle bundle = new PatientRecordBundle();

        for (PatientRecord.RecordType type : PatientRecord.RecordType.values()) {
            try {
                List<PatientRecord> records = getRecords(patientId, type);
                bundle.addRecords(type, records);
            } catch (AccessDeniedException e) {
                bundle.addDenied(type, e.getMessage());
            }
        }

        return bundle;
    }

    /**
     * Search patients by name.
     */
    public List<Patient> searchPatients(String query) {
        AppUserDetails user = getCurrentUser();

        accessLogService.logAccess(new AccessLog()
            .userId(user.getUserId())
            .usernameAttempted(user.getUsername())
            .action("SEARCH")
            .resourceType("PATIENT")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Patient search: " + query));

        return patientRepo.findAll().stream()
            .filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
            .toList();
    }

    private AppUserDetails getCurrentUser() {
        return (AppUserDetails) SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
    }

    /** Thrown when RBAC denies access */
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }

    /** Bundle of records grouped by type, with denial info */
    public static class PatientRecordBundle {
        private final java.util.Map<PatientRecord.RecordType, List<PatientRecord>> records = new java.util.EnumMap<>(PatientRecord.RecordType.class);
        private final java.util.Map<PatientRecord.RecordType, String> denied = new java.util.EnumMap<>(PatientRecord.RecordType.class);

        public void addRecords(PatientRecord.RecordType type, List<PatientRecord> recs) { records.put(type, recs); }
        public void addDenied(PatientRecord.RecordType type, String reason) { denied.put(type, reason); }
        public java.util.Map<PatientRecord.RecordType, List<PatientRecord>> getRecords() { return records; }
        public java.util.Map<PatientRecord.RecordType, String> getDenied() { return denied; }
    }
}
