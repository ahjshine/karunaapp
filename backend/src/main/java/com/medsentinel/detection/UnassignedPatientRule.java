package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.patient.CareAssignmentRepository;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * R5: Unassigned patient access — accessing a patient not in the user's
 * ward or care team, without break-glass authorization.
 */
@Component
public class UnassignedPatientRule implements DetectionRule {

    private final UserRepository userRepo;
    private final PatientRepository patientRepo;
    private final CareAssignmentRepository careAssignmentRepo;

    public UnassignedPatientRule(UserRepository userRepo,
                                  PatientRepository patientRepo,
                                  CareAssignmentRepository careAssignmentRepo) {
        this.userRepo = userRepo;
        this.patientRepo = patientRepo;
        this.careAssignmentRepo = careAssignmentRepo;
    }

    @Override
    public String getRuleCode() { return "R5"; }

    @Override
    public String getRuleName() { return "Unassigned Patient Access"; }

    @Override
    public int getPoints() { return 15; }

    @Override
    public List<RuleHit> evaluate(AccessLog triggeringEntry) {
        if (triggeringEntry.getUserId() == null || triggeringEntry.getPatientId() == null) {
            return Collections.emptyList();
        }
        if (triggeringEntry.getOutcome() != AccessLog.Outcome.ALLOWED) {
            return Collections.emptyList();
        }
        // Don't flag break-glass events
        if ("BREAK_GLASS".equals(triggeringEntry.getAction())) {
            return Collections.emptyList();
        }

        Long userId = triggeringEntry.getUserId();
        Long patientId = triggeringEntry.getPatientId();

        User user = userRepo.findById(userId).orElse(null);
        Patient patient = patientRepo.findById(patientId).orElse(null);
        if (user == null || patient == null) return Collections.emptyList();

        // Check if patient is on user's ward
        boolean onWard = user.getWardId() != null && user.getWardId().equals(patient.getWardId());
        boolean assigned = careAssignmentRepo.existsByUserIdAndPatientId(userId, patientId);

        if (onWard || assigned) return Collections.emptyList();

        return List.of(new RuleHit(
            "R5", userId, user.getUsername(), 15,
            String.format("%s (%s) accessed patient '%s' who is not in their ward or care assignments",
                user.getUsername(), user.getRole().getName(), patient.getName()),
            List.of(triggeringEntry.getLogId())
        ));
    }
}
