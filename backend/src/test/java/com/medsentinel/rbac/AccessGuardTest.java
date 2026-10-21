package com.medsentinel.rbac;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.patient.CareAssignmentRepository;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRecord;
import com.medsentinel.patient.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccessGuardTest {

    private CareAssignmentRepository careRepo;
    private PatientRepository patientRepo;
    private AccessGuard guard;

    @BeforeEach
    void setUp() {
        careRepo = mock(CareAssignmentRepository.class);
        patientRepo = mock(PatientRepository.class);
        guard = new AccessGuard(careRepo, patientRepo);
    }

    @Test
    void doctor_HasClinicalAndExport_NoBilling() {
        AppUserDetails doctor = new AppUserDetails(1L, "dr.asha", "pass", "DOCTOR", 1L,
            List.of(
                new SimpleGrantedAuthority("ROLE_DOCTOR"),
                new SimpleGrantedAuthority("VIEW_DEMOGRAPHICS"),
                new SimpleGrantedAuthority("VIEW_CLINICAL"),
                new SimpleGrantedAuthority("EXPORT")
            ));

        assertNull(guard.checkAccess(doctor, PatientRecord.RecordType.CLINICAL, 1L));
        assertNull(guard.checkAccess(doctor, PatientRecord.RecordType.DEMOGRAPHIC, 1L));
        assertNull(guard.checkExport(doctor));

        // Denied billing
        String billingDenial = guard.checkAccess(doctor, PatientRecord.RecordType.BILLING, 1L);
        assertNotNull(billingDenial);
        assertTrue(billingDenial.contains("lacks permission VIEW_BILLING"));
    }

    @Test
    void nurse_OwnWardClinicalAllowed_OffWardClinicalDenied() {
        AppUserDetails nurse = new AppUserDetails(2L, "nurse.priya", "pass", "NURSE", 1L,
            List.of(
                new SimpleGrantedAuthority("ROLE_NURSE"),
                new SimpleGrantedAuthority("VIEW_DEMOGRAPHICS"),
                new SimpleGrantedAuthority("VIEW_CLINICAL")
            ));

        Patient onWardPatient = new Patient();
        onWardPatient.setPatientId(1L);
        onWardPatient.setWardId(1L);

        Patient offWardPatient = new Patient();
        offWardPatient.setPatientId(15L);
        offWardPatient.setWardId(2L);

        when(patientRepo.findById(1L)).thenReturn(Optional.of(onWardPatient));
        when(patientRepo.findById(15L)).thenReturn(Optional.of(offWardPatient));
        when(careRepo.existsByUserIdAndPatientId(2L, 15L)).thenReturn(false);

        // Own ward allowed
        assertNull(guard.checkAccess(nurse, PatientRecord.RecordType.CLINICAL, 1L));

        // Off ward denied
        String denial = guard.checkAccess(nurse, PatientRecord.RecordType.CLINICAL, 15L);
        assertNotNull(denial);
        assertTrue(denial.contains("Nurse not assigned to patient's ward"));

        // Export denied
        assertNotNull(guard.checkExport(nurse));
    }

    @Test
    void billingClerk_BillingAllowed_ClinicalDenied() {
        AppUserDetails clerk = new AppUserDetails(3L, "clerk.ravi", "pass", "BILLING", null,
            List.of(
                new SimpleGrantedAuthority("ROLE_BILLING"),
                new SimpleGrantedAuthority("VIEW_DEMOGRAPHICS"),
                new SimpleGrantedAuthority("VIEW_BILLING")
            ));

        assertNull(guard.checkAccess(clerk, PatientRecord.RecordType.BILLING, 1L));
        assertNull(guard.checkAccess(clerk, PatientRecord.RecordType.DEMOGRAPHIC, 1L));

        String clinicalDenial = guard.checkAccess(clerk, PatientRecord.RecordType.CLINICAL, 1L);
        assertNotNull(clinicalDenial);
        assertTrue(clinicalDenial.contains("lacks permission VIEW_CLINICAL"));
    }

    @Test
    void receptionist_DemographicsAllowed_ClinicalDenied() {
        AppUserDetails rec = new AppUserDetails(4L, "rec.neha", "pass", "RECEPTIONIST", null,
            List.of(
                new SimpleGrantedAuthority("ROLE_RECEPTIONIST"),
                new SimpleGrantedAuthority("VIEW_DEMOGRAPHICS")
            ));

        assertNull(guard.checkAccess(rec, PatientRecord.RecordType.DEMOGRAPHIC, 1L));

        String clinicalDenial = guard.checkAccess(rec, PatientRecord.RecordType.CLINICAL, 1L);
        assertNotNull(clinicalDenial);
        assertTrue(clinicalDenial.contains("VIEW_CLINICAL"));
    }
}
