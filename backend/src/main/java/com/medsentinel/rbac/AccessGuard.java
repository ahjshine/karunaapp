package com.medsentinel.rbac;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.patient.CareAssignmentRepository;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.patient.PatientRecord;

/**
 * Central RBAC enforcement: checks whether a user can access a given resource.
 */
public class AccessGuard {

    private final CareAssignmentRepository careAssignmentRepo;
    private final PatientRepository patientRepo;

    public AccessGuard() {
        this(new CareAssignmentRepository(), new PatientRepository());
    }

    public AccessGuard(CareAssignmentRepository careAssignmentRepo,
                       PatientRepository patientRepo) {
        this.careAssignmentRepo = careAssignmentRepo;
        this.patientRepo = patientRepo;
    }

    /**
     * Check if user has permission to view a record of the given type.
     * Returns null if allowed, or a denial reason string if denied.
     */
    public String checkAccess(AppUserDetails user, PatientRecord.RecordType recordType,
                               Long patientId) {
        if (user == null) {
            return "Authenticated principal required";
        }
        if (recordType == null) {
            return "Record type is required";
        }

        // Map record type to required permission
        String requiredPerm = switch (recordType) {
            case CLINICAL -> "VIEW_CLINICAL";
            case BILLING -> "VIEW_BILLING";
            case DEMOGRAPHIC -> "VIEW_DEMOGRAPHICS";
        };

        if (!user.hasPermission(requiredPerm)) {
            return "Role " + user.getRoleName() + " lacks permission " + requiredPerm;
        }

        // Nurse: clinical access restricted to own ward patients
        if ("NURSE".equals(user.getRoleName()) && recordType == PatientRecord.RecordType.CLINICAL) {
            if (patientId != null && user.getWardId() != null) {
                Patient patient = patientRepo.findById(patientId).orElse(null);
                if (patient != null && patient.getWardId() != null
                    && !patient.getWardId().equals(user.getWardId())) {
                    // Check care assignment as fallback
                    if (!careAssignmentRepo.existsByUserIdAndPatientId(user.getUserId(), patientId)) {
                        return "Nurse not assigned to patient's ward and no care assignment";
                    }
                }
            }
        }

        return null; // Access allowed
    }

    /**
     * Check if user can export data.
     */
    public String checkExport(AppUserDetails user) {
        if (!user.hasPermission("EXPORT")) {
            return "Role " + user.getRoleName() + " lacks EXPORT permission";
        }
        return null;
    }

    /**
     * Check if the patient is on the user's ward or in their care assignments.
     */
    public boolean isOnWardOrAssigned(Long userId, Long patientId, Long userWardId) {
        if (userWardId != null) {
            Patient patient = patientRepo.findById(patientId).orElse(null);
            if (patient != null && patient.getWardId().equals(userWardId)) {
                return true;
            }
        }
        return careAssignmentRepo.existsByUserIdAndPatientId(userId, patientId);
    }
}
