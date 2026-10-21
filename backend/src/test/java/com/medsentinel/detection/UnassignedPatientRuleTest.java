package com.medsentinel.detection;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.patient.CareAssignmentRepository;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.rbac.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnassignedPatientRuleTest {

    private UserRepository userRepo;
    private PatientRepository patientRepo;
    private CareAssignmentRepository careAssignmentRepo;
    private UnassignedPatientRule rule;

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepository.class);
        patientRepo = mock(PatientRepository.class);
        careAssignmentRepo = mock(CareAssignmentRepository.class);
        rule = new UnassignedPatientRule(userRepo, patientRepo, careAssignmentRepo);

        Role role = new Role("DOCTOR");
        role.setRoleId(1L);

        User doctor = new User();
        doctor.setUserId(1L);
        doctor.setUsername("dr.asha");
        doctor.setRole(role);
        doctor.setWardId(1L); // Cardiology ward

        when(userRepo.findById(1L)).thenReturn(Optional.of(doctor));
    }

    @Test
    void unassignedOffWardPatientAccess_GeneratesR5Hit() {
        Patient neurologyPatient = new Patient();
        neurologyPatient.setPatientId(15L);
        neurologyPatient.setName("Mohan Das");
        neurologyPatient.setWardId(2L); // Different ward

        when(patientRepo.findById(15L)).thenReturn(Optional.of(neurologyPatient));
        when(careAssignmentRepo.existsByUserIdAndPatientId(1L, 15L)).thenReturn(false);

        AccessLog log = new AccessLog().userId(1L).usernameAttempted("dr.asha")
            .action("VIEW").outcome(AccessLog.Outcome.ALLOWED).patientId(15L);
        log.setLogId(101L);

        List<RuleHit> hits = rule.evaluate(log);

        assertEquals(1, hits.size());
        assertEquals("R5", hits.get(0).getRuleCode());
        assertEquals(15, hits.get(0).getPoints());
        assertTrue(hits.get(0).getExplanation().contains("Mohan Das"));
        assertEquals(List.of(101L), hits.get(0).getEvidenceLogIds());
    }

    @Test
    void onWardPatientAccess_GeneratesNoHit() {
        Patient cardioPatient = new Patient();
        cardioPatient.setPatientId(2L);
        cardioPatient.setName("Sunita Sharma");
        cardioPatient.setWardId(1L); // Same ward

        when(patientRepo.findById(2L)).thenReturn(Optional.of(cardioPatient));

        AccessLog log = new AccessLog().userId(1L).usernameAttempted("dr.asha")
            .action("VIEW").outcome(AccessLog.Outcome.ALLOWED).patientId(2L);
        log.setLogId(102L);

        List<RuleHit> hits = rule.evaluate(log);
        assertTrue(hits.isEmpty());
    }

    @Test
    void breakGlassAction_IsExemptFromR5() {
        AccessLog log = new AccessLog().userId(1L).usernameAttempted("dr.asha")
            .action("BREAK_GLASS").outcome(AccessLog.Outcome.ALLOWED).patientId(15L);

        List<RuleHit> hits = rule.evaluate(log);
        assertTrue(hits.isEmpty(), "Break-glass should not be flagged as unauthorized R5 access");
    }
}
