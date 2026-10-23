package com.medsentinel.simulator;

import com.medsentinel.alert.AlertService;
import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.rbac.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScenarioRunnerTest {

    private AccessLogService accessLogService;
    private AlertService alertService;
    private UserRepository userRepo;
    private PatientRepository patientRepo;
    private AccessLogRepository logRepo;
    private ScenarioRunner runner;

    private User nurse;
    private User doctor;
    private User clerk;
    private User receptionist;
    private List<Patient> patients;

    @BeforeEach
    void setUp() {
        accessLogService = mock(AccessLogService.class);
        alertService = mock(AlertService.class);
        userRepo = mock(UserRepository.class);
        patientRepo = mock(PatientRepository.class);
        logRepo = mock(AccessLogRepository.class);

        runner = new ScenarioRunner(accessLogService, alertService, userRepo, patientRepo, logRepo);

        Role nurseRole = new Role("NURSE");
        nurseRole.setRoleId(2L);
        nurse = new User();
        nurse.setUserId(2L);
        nurse.setUsername("nurse.priya");
        nurse.setRole(nurseRole);
        nurse.setWardId(1L);

        Role docRole = new Role("DOCTOR");
        docRole.setRoleId(1L);
        doctor = new User();
        doctor.setUserId(1L);
        doctor.setUsername("dr.asha");
        doctor.setRole(docRole);
        doctor.setWardId(1L);

        Role clerkRole = new Role("BILLING");
        clerkRole.setRoleId(3L);
        clerk = new User();
        clerk.setUserId(3L);
        clerk.setUsername("clerk.ravi");
        clerk.setRole(clerkRole);

        Role recRole = new Role("RECEPTIONIST");
        recRole.setRoleId(4L);
        receptionist = new User();
        receptionist.setUserId(4L);
        receptionist.setUsername("rec.neha");
        receptionist.setRole(recRole);

        when(userRepo.findByUsername("nurse.priya")).thenReturn(Optional.of(nurse));
        when(userRepo.findByUsername("dr.asha")).thenReturn(Optional.of(doctor));
        when(userRepo.findByUsername("clerk.ravi")).thenReturn(Optional.of(clerk));
        when(userRepo.findByUsername("rec.neha")).thenReturn(Optional.of(receptionist));

        patients = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            Patient p = new Patient();
            p.setPatientId(i);
            p.setName("Cardio Patient " + i);
            p.setWardId(1L);
            patients.add(p);
        }
        for (long i = 11; i <= 20; i++) {
            Patient p = new Patient();
            p.setPatientId(i);
            p.setName("Neuro Patient " + i);
            p.setWardId(2L);
            patients.add(p);
        }

        when(patientRepo.findByWardId(1L)).thenReturn(patients.subList(0, 10));
        when(patientRepo.findAll()).thenReturn(patients);

        AccessLog mockLog = new AccessLog();
        mockLog.setLogId(1L);
        when(accessLogService.logAccess(any(AccessLog.class))).thenReturn(mockLog);
    }

    @Test
    void normalNurseShift_LogsAllowedViews() {
        String result = runner.runNormalNurseShift();
        assertTrue(result.contains("Normal nurse shift"));
        verify(accessLogService, times(10)).logAccess(argThat(log ->
            "nurse.priya".equals(log.getUsernameAttempted()) &&
            log.getOutcome() == AccessLog.Outcome.ALLOWED
        ));
    }

    @Test
    void busyClinicianBurst_LogsRapidViews() {
        String result = runner.runBusyClinician();
        assertTrue(result.contains("Busy clinician burst"));
        verify(accessLogService, times(40)).logAccess(argThat(log ->
            "nurse.priya".equals(log.getUsernameAttempted()) &&
            log.getOutcome() == AccessLog.Outcome.ALLOWED
        ));
    }

    @Test
    void bruteForceLogin_Logs6FailuresAnd1Success() {
        String result = runner.runBruteForceLogin();
        assertTrue(result.contains("Brute-force login"));
        verify(accessLogService, times(6)).logAccess(argThat(log ->
            "dr.asha".equals(log.getUsernameAttempted()) &&
            log.getOutcome() == AccessLog.Outcome.FAILED
        ));
        verify(accessLogService, times(1)).logAccess(argThat(log ->
            "dr.asha".equals(log.getUsernameAttempted()) &&
            log.getOutcome() == AccessLog.Outcome.ALLOWED
        ));
    }

    @Test
    void billingClerkBulkPull_LogsDeniedClinicalAndAllowedBilling() {
        String result = runner.runBillingClerkBulkPull();
        assertTrue(result.contains("Billing clerk bulk pull"));
        // 5 clinical denied + 38 billing allowed = 43 logs
        verify(accessLogService, times(5)).logAccess(argThat(log ->
            "clerk.ravi".equals(log.getUsernameAttempted()) &&
            "CLINICAL".equals(log.getResourceType()) &&
            log.getOutcome() == AccessLog.Outcome.DENIED
        ));
        verify(accessLogService, times(38)).logAccess(argThat(log ->
            "clerk.ravi".equals(log.getUsernameAttempted()) &&
            "BILLING".equals(log.getResourceType()) &&
            log.getOutcome() == AccessLog.Outcome.ALLOWED
        ));
    }

    @Test
    void receptionistReadsClinical_LogsDeniedAccess() {
        String result = runner.runReceptionistReadsClinical();
        assertTrue(result.contains("denied"));
        verify(accessLogService, times(1)).logAccess(argThat(log ->
            "rec.neha".equals(log.getUsernameAttempted()) &&
            "CLINICAL".equals(log.getResourceType()) &&
            log.getOutcome() == AccessLog.Outcome.DENIED
        ));
    }

    @Test
    void breakGlass_LogsAndCreatesMediumAlert() {
        String result = runner.runBreakGlass();
        assertTrue(result.contains("Break-glass"));
        verify(accessLogService).logAccess(argThat(log ->
            "BREAK_GLASS".equals(log.getAction()) &&
            "dr.asha".equals(log.getUsernameAttempted())
        ));
        verify(alertService).createOrMergeAlert(
            eq(1L), eq("BREAK_GLASS"), eq(50), eq("MEDIUM"),
            contains("Emergency consult"), any()
        );
    }
}
