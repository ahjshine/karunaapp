package com.medsentinel.integration;

import com.medsentinel.alert.Alert;
import com.medsentinel.alert.AlertRepository;
import com.medsentinel.alert.AlertService;
import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.breakglass.BreakGlassService;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.logging.LogChainService;
import com.medsentinel.patient.*;
import com.medsentinel.rbac.*;
import com.medsentinel.simulator.ScenarioRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MedSentinelIntegrationTest {

    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;
    @Autowired private PermissionRepository permRepo;
    @Autowired private RoleBaselineRepository baselineRepo;
    @Autowired private WardRepository wardRepo;
    @Autowired private PatientRepository patientRepo;
    @Autowired private RecordRepository recordRepo;
    @Autowired private CareAssignmentRepository careRepo;
    @Autowired private AccessLogRepository logRepo;
    @Autowired private AlertRepository alertRepo;
    @Autowired private AccessLogService accessLogService;
    @Autowired private LogChainService chainService;
    @Autowired private AlertService alertService;
    @Autowired private PatientService patientService;
    @Autowired private BreakGlassService breakGlassService;
    @Autowired private ScenarioRunner scenarioRunner;

    private User doctor;
    private User nurse;
    private User clerk;
    private Patient cardioPatient;
    private Patient neuroPatient;

    @BeforeEach
    void setUp() {
        alertRepo.deleteAll();
        logRepo.deleteAll();
        careRepo.deleteAll();
        recordRepo.deleteAll();
        patientRepo.deleteAll();
        userRepo.deleteAll();
        baselineRepo.deleteAll();
        roleRepo.deleteAll();
        permRepo.deleteAll();
        wardRepo.deleteAll();

        // Wards
        Ward cardio = new Ward();
        cardio.setName("Cardiology");
        cardio = wardRepo.save(cardio);

        Ward neuro = new Ward();
        neuro.setName("Neurology");
        neuro = wardRepo.save(neuro);

        // Roles & Permissions
        Permission pDemog = permRepo.save(new Permission("VIEW_DEMOGRAPHICS"));
        Permission pClin = permRepo.save(new Permission("VIEW_CLINICAL"));
        Permission pBill = permRepo.save(new Permission("VIEW_BILLING"));
        Permission pExport = permRepo.save(new Permission("EXPORT"));

        Role rDoc = new Role("DOCTOR");
        rDoc.getPermissions().addAll(List.of(pDemog, pClin, pExport));
        rDoc = roleRepo.save(rDoc);

        Role rNurse = new Role("NURSE");
        rNurse.getPermissions().addAll(List.of(pDemog, pClin));
        rNurse = roleRepo.save(rNurse);

        Role rClerk = new Role("BILLING");
        rClerk.getPermissions().addAll(List.of(pDemog, pBill));
        rClerk = roleRepo.save(rClerk);

        Role rRec = new Role("RECEPTIONIST");
        rRec.getPermissions().add(pDemog);
        rRec = roleRepo.save(rRec);

        // Baselines
        RoleBaseline nurseOnWard = new RoleBaseline();
        nurseOnWard.setRoleId(rNurse.getRoleId());
        nurseOnWard.setScope(RoleBaseline.BaselineScope.ON_WARD);
        nurseOnWard.setMaxRecordsPer10Min(50);
        nurseOnWard.setMultiplier(BigDecimal.valueOf(1.5));
        baselineRepo.save(nurseOnWard);

        RoleBaseline nurseOffWard = new RoleBaseline();
        nurseOffWard.setRoleId(rNurse.getRoleId());
        nurseOffWard.setScope(RoleBaseline.BaselineScope.OFF_WARD);
        nurseOffWard.setMaxRecordsPer10Min(5);
        nurseOffWard.setMultiplier(BigDecimal.valueOf(1.5));
        baselineRepo.save(nurseOffWard);

        RoleBaseline clerkAny = new RoleBaseline();
        clerkAny.setRoleId(rClerk.getRoleId());
        clerkAny.setScope(RoleBaseline.BaselineScope.ANY);
        clerkAny.setMaxRecordsPer10Min(15);
        clerkAny.setMultiplier(BigDecimal.valueOf(1.5));
        baselineRepo.save(clerkAny);

        // Users
        doctor = new User();
        doctor.setUsername("dr.asha");
        doctor.setPwHash("hashedpass");
        doctor.setRole(rDoc);
        doctor.setWardId(cardio.getWardId());
        doctor.setShiftStart(LocalTime.of(8, 0));
        doctor.setShiftEnd(LocalTime.of(20, 0));
        doctor = userRepo.save(doctor);

        nurse = new User();
        nurse.setUsername("nurse.priya");
        nurse.setPwHash("hashedpass");
        nurse.setRole(rNurse);
        nurse.setWardId(cardio.getWardId());
        nurse.setShiftStart(LocalTime.of(8, 0));
        nurse.setShiftEnd(LocalTime.of(20, 0));
        nurse = userRepo.save(nurse);

        clerk = new User();
        clerk.setUsername("clerk.ravi");
        clerk.setPwHash("hashedpass");
        clerk.setRole(rClerk);
        clerk.setShiftStart(LocalTime.of(9, 0));
        clerk.setShiftEnd(LocalTime.of(18, 0));
        clerk = userRepo.save(clerk);

        // Patients
        cardioPatient = new Patient();
        cardioPatient.setName("Arjun Mehta");
        cardioPatient.setDob(LocalDate.of(1985, 3, 15));
        cardioPatient.setWardId(cardio.getWardId());
        cardioPatient = patientRepo.save(cardioPatient);

        neuroPatient = new Patient();
        neuroPatient.setName("Karthik Rajan");
        neuroPatient.setDob(LocalDate.of(1992, 5, 17));
        neuroPatient.setWardId(neuro.getWardId());
        neuroPatient = patientRepo.save(neuroPatient);

        // Records
        PatientRecord clinRec = new PatientRecord();
        clinRec.setPatientId(cardioPatient.getPatientId());
        clinRec.setType(PatientRecord.RecordType.CLINICAL);
        clinRec.setContent("BP 120/80 - stable");
        recordRepo.save(clinRec);

        PatientRecord billRec = new PatientRecord();
        billRec.setPatientId(cardioPatient.getPatientId());
        billRec.setType(PatientRecord.RecordType.BILLING);
        billRec.setContent("Consultation Rs 500");
        recordRepo.save(billRec);
    }

    private void authenticate(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRole().getPermissions().stream()
            .map(p -> new SimpleGrantedAuthority(p.getCode()))
            .toList();
        AppUserDetails details = new AppUserDetails(
            user.getUserId(), user.getUsername(), user.getPwHash(),
            user.getRole().getName(), user.getWardId(),
            new java.util.ArrayList<>(authorities)
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities())
        );
    }

    @Test
    @DisplayName("T1 & T2: 5 failed logins triggers R1 (Low+), followed by success triggers R1b (Medium)")
    void testFailedLoginsAndBruteForce() {
        for (int i = 0; i < 5; i++) {
            accessLogService.logAccess(new AccessLog()
                .usernameAttempted("dr.asha")
                .action("LOGIN")
                .outcome(AccessLog.Outcome.FAILED)
                .reason("Bad credentials"));
        }

        List<Alert> alerts = alertRepo.findOpenAlerts();
        assertFalse(alerts.isEmpty(), "5 failed logins must produce an alert");
        Alert r1Alert = alerts.get(0);
        assertTrue(r1Alert.getRuleCodes().contains("R1"));
        assertTrue(r1Alert.getScore() >= 30);

        // Now success right after 5 failures -> triggers R1b (+25 points)
        accessLogService.logAccess(new AccessLog()
            .userId(doctor.getUserId())
            .usernameAttempted("dr.asha")
            .action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Success after failure"));

        List<Alert> updatedAlerts = alertRepo.findOpenAlerts();
        boolean hasR1b = updatedAlerts.stream().anyMatch(a -> a.getRuleCodes().contains("R1b"));
        assertTrue(hasR1b, "Successful login after brute-force must trigger R1b");
    }

    @Test
    @DisplayName("T3: Billing clerk requesting clinical note is denied (403) and triggers R3 alert")
    void testClerkDeniedClinical() {
        authenticate(clerk);

        assertThrows(PatientService.AccessDeniedException.class, () ->
            patientService.getRecords(cardioPatient.getPatientId(), PatientRecord.RecordType.CLINICAL)
        );

        List<Alert> alerts = alertRepo.findOpenAlerts();
        assertFalse(alerts.isEmpty(), "Unauthorized clinical access must trigger alert");
        Alert alert = alerts.get(0);
        assertTrue(alert.getRuleCodes().contains("R3"));
        assertEquals(40, alert.getScore());
        assertEquals(Alert.Severity.LOW, alert.getSeverity());
    }

    @Test
    @DisplayName("T4: Billing clerk pulling 38 records produces High alert (R2 + R3)")
    void testBillingClerkBulkPullProducesHighAlert() {
        // Trigger R3 first via denied clinical access
        authenticate(clerk);
        try {
            patientService.getRecords(cardioPatient.getPatientId(), PatientRecord.RecordType.CLINICAL);
        } catch (Exception ignored) {}

        // Then pull 38 billing records (exceeds ANY baseline = 15 * 1.5 = 22)
        for (int i = 0; i < 38; i++) {
            accessLogService.logAccess(new AccessLog()
                .userId(clerk.getUserId())
                .usernameAttempted(clerk.getUsername())
                .action("VIEW")
                .resourceType("BILLING")
                .patientId(cardioPatient.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Billing check"));
        }

        List<Alert> alerts = alertRepo.findOpenAlerts();
        assertFalse(alerts.isEmpty());
        Alert clerkAlert = alerts.stream()
            .filter(a -> clerk.getUserId().equals(a.getUserId()))
            .findFirst().orElseThrow();

        assertTrue(clerkAlert.getScore() >= 70, "Combined R2 + R3 score must be >= 70");
        assertEquals(Alert.Severity.HIGH, clerkAlert.getSeverity(), "Combined alert must be HIGH severity");
        assertTrue(clerkAlert.getRuleCodes().contains("R2"));
        assertTrue(clerkAlert.getRuleCodes().contains("R3"));
    }

    @Test
    @DisplayName("T5: Nurse opening 40 own-ward charts produces NO alert (busy-clinician guard)")
    void testBusyClinicianGuard() {
        authenticate(nurse);

        for (int i = 0; i < 40; i++) {
            accessLogService.logAccess(new AccessLog()
                .userId(nurse.getUserId())
                .usernameAttempted(nurse.getUsername())
                .action("VIEW")
                .resourceType("CLINICAL")
                .patientId(cardioPatient.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Ward check"));
        }

        List<Alert> nurseAlerts = alertRepo.findOpenAlerts().stream()
            .filter(a -> nurse.getUserId().equals(a.getUserId()))
            .toList();

        assertTrue(nurseAlerts.isEmpty(), "Busy clinician burst on own ward must produce NO alert (AC4 / T5)");
    }

    @Test
    @DisplayName("T6: Nurse opening 40 off-ward charts produces R2 (+R5) alert")
    void testNurseOffWardProducesAlert() {
        authenticate(nurse);

        for (int i = 0; i < 40; i++) {
            accessLogService.logAccess(new AccessLog()
                .userId(nurse.getUserId())
                .usernameAttempted(nurse.getUsername())
                .action("VIEW")
                .resourceType("CLINICAL")
                .patientId(neuroPatient.getPatientId())
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Off-ward chart review"));
        }

        List<Alert> nurseAlerts = alertRepo.findOpenAlerts().stream()
            .filter(a -> nurse.getUserId().equals(a.getUserId()))
            .toList();

        assertFalse(nurseAlerts.isEmpty(), "Nurse accessing off-ward records above baseline must trigger alert");
        assertTrue(nurseAlerts.get(0).getRuleCodes().contains("R2"));
    }

    @Test
    @DisplayName("T7: Doctor break-glass grants access and produces Medium review alert")
    void testDoctorBreakGlass() {
        authenticate(doctor);

        String justification = "Acute cardiac arrest in neuro ward, urgent consult";
        AppUserDetails details = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BreakGlassService.BreakGlassResult result = breakGlassService.executeBreakGlass(
            details, neuroPatient.getPatientId(), justification
        );

        assertNotNull(result);
        assertEquals(doctor.getUserId(), result.event().getUserId());
        assertEquals(neuroPatient.getPatientId(), result.event().getPatientId());

        List<Alert> alerts = alertRepo.findOpenAlerts();
        boolean hasBreakGlassAlert = alerts.stream().anyMatch(a ->
            "BREAK_GLASS".equals(a.getRuleCodes()) &&
            a.getSeverity() == Alert.Severity.MEDIUM &&
            a.getScore() == 50
        );
        assertTrue(hasBreakGlassAlert, "Break glass must create a Medium review alert (score 50)");
    }

    @Test
    @DisplayName("T8: Hash chain verification reports OK for intact logs, and detects tampering")
    void testHashChainIntegrity() {
        // Generate valid logs
        for (int i = 1; i <= 5; i++) {
            accessLogService.logAccess(new AccessLog()
                .userId(doctor.getUserId())
                .usernameAttempted("dr.asha")
                .action("VIEW")
                .outcome(AccessLog.Outcome.ALLOWED)
                .reason("Routine visit " + i));
        }

        // Verify chain is intact
        LogChainService.ChainVerificationResult intactResult = chainService.verifyChain();
        assertTrue(intactResult.intact(), "Unhampered logs must pass verification");
        assertEquals(5, intactResult.rowsChecked());

        // Tamper row 3's hash
        List<AccessLog> allLogs = logRepo.findAllOrderedById();
        AccessLog tampered = allLogs.get(2);
        tampered.setRowHash("tampered_hash_value_1234567890abcdef1234567890abcdef1234567890abcdef");
        logRepo.save(tampered);

        // Verify tampering is detected
        LogChainService.ChainVerificationResult brokenResult = chainService.verifyChain();
        assertFalse(brokenResult.intact(), "Tampered log must be detected");
        assertEquals(tampered.getLogId(), brokenResult.brokenAtLogId(),
            "Integrity check must report the exact broken log_id");
    }
}
