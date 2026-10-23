package com.medsentinel.patient;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.rbac.AccessGuard;
import com.medsentinel.security.AuthenticationRequiredException;

import java.util.List;

/**
 * Single entry point for patient data access.
 * Enforces RBAC and logs every attempt.
 */
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
        return listPatients(requireCurrentUser());
    }

    public List<Patient> listPatients(AppUserDetails user) {
        user = requireUser(user);
        requireDemographicPermission(user);
        return patientRepo.findAll();
    }

    public Patient getPatient(Long patientId) {
        return getPatient(requireCurrentUser(), patientId);
    }

    public Patient getPatient(AppUserDetails user, Long patientId) {
        user = requireUser(user);
        requireDemographicPermission(user);
        return patientRepo.findById(patientId).orElse(null);
    }

    public List<PatientRecord> getRecords(Long patientId, PatientRecord.RecordType type) {
        return getRecords(requireCurrentUser(), patientId, type);
    }

    public List<PatientRecord> getRecords(AppUserDetails user, Long patientId, PatientRecord.RecordType type) {
        user = requireUser(user);
        String ip = "127.0.0.1";

        // Check permission
        String denialReason = accessGuard.checkAccess(user, type, patientId);

        if (denialReason != null) {
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

        List<PatientRecord> records = recordRepo.findByPatientIdAndType(patientId, type);

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

    public PatientRecordBundle getAccessibleRecords(Long patientId) {
        return getAccessibleRecords(requireCurrentUser(), patientId);
    }

    public PatientRecordBundle getAccessibleRecords(AppUserDetails user, Long patientId) {
        user = requireUser(user);
        requireDemographicPermission(user);
        PatientRecordBundle bundle = new PatientRecordBundle();

        for (PatientRecord.RecordType type : PatientRecord.RecordType.values()) {
            try {
                List<PatientRecord> records = getRecords(user, patientId, type);
                bundle.addRecords(type, records);
            } catch (AccessDeniedException e) {
                bundle.addDenied(type, e.getMessage());
            }
        }

        return bundle;
    }

    public List<Patient> searchPatients(String query) {
        return searchPatients(requireCurrentUser(), query);
    }

    public List<Patient> searchPatients(AppUserDetails user, String query) {
        user = requireUser(user);
        requireDemographicPermission(user);

        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.isEmpty()) {
            return List.of();
        }

        accessLogService.logAccess(new AccessLog()
            .userId(user.getUserId())
            .usernameAttempted(user.getUsername())
            .action("SEARCH")
            .resourceType("PATIENT")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Patient search: " + normalizedQuery));

        return patientRepo.findByNameContaining(normalizedQuery);
    }

    // ThreadLocal holder for user context when needed
    private static final ThreadLocal<AppUserDetails> currentThreadUser = new ThreadLocal<>();

    public static void setCurrentUser(AppUserDetails user) {
        currentThreadUser.set(user);
    }

    public static AppUserDetails getCurrentUser() {
        return currentThreadUser.get();
    }

    public static AppUserDetails requireCurrentUser() {
        AppUserDetails user = currentThreadUser.get();
        if (user == null) {
            throw new AuthenticationRequiredException();
        }
        return user;
    }

    public static void clearCurrentUser() {
        currentThreadUser.remove();
    }

    private AppUserDetails requireUser(AppUserDetails user) {
        if (user == null) {
            throw new AuthenticationRequiredException();
        }
        return user;
    }

    private void requireDemographicPermission(AppUserDetails user) {
        if (!user.hasPermission("VIEW_DEMOGRAPHICS") && !user.hasRole("ADMIN")) {
            throw new AccessDeniedException("Role " + user.getRoleName() + " lacks permission VIEW_DEMOGRAPHICS");
        }
    }

    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) {
            super(message);
        }
    }

    public static class PatientRecordBundle {
        private final java.util.Map<PatientRecord.RecordType, List<PatientRecord>> records = new java.util.EnumMap<>(PatientRecord.RecordType.class);
        private final java.util.Map<PatientRecord.RecordType, String> denied = new java.util.EnumMap<>(PatientRecord.RecordType.class);

        public void addRecords(PatientRecord.RecordType type, List<PatientRecord> recs) { records.put(type, recs); }
        public void addDenied(PatientRecord.RecordType type, String reason) { denied.put(type, reason); }
        public java.util.Map<PatientRecord.RecordType, List<PatientRecord>> getRecords() { return records; }
        public java.util.Map<PatientRecord.RecordType, String> getDenied() { return denied; }
    }
}
