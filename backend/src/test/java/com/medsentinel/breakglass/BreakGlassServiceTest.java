package com.medsentinel.breakglass;

import com.medsentinel.alert.AlertService;
import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.patient.Patient;
import com.medsentinel.patient.PatientRecord;
import com.medsentinel.patient.PatientRepository;
import com.medsentinel.patient.RecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BreakGlassServiceTest {

    private BreakGlassRepository breakGlassRepo;
    private PatientRepository patientRepo;
    private RecordRepository recordRepo;
    private AccessLogService accessLogService;
    private AlertService alertService;
    private BreakGlassService service;

    private AppUserDetails doctor;
    private AppUserDetails nurse;
    private Patient patient;

    @BeforeEach
    void setUp() {
        breakGlassRepo = mock(BreakGlassRepository.class);
        patientRepo = mock(PatientRepository.class);
        recordRepo = mock(RecordRepository.class);
        accessLogService = mock(AccessLogService.class);
        alertService = mock(AlertService.class);

        service = new BreakGlassService(breakGlassRepo, patientRepo, recordRepo, accessLogService, alertService);

        doctor = new AppUserDetails(1L, "dr.asha", "pass", "DOCTOR", 1L,
            List.of("ROLE_DOCTOR", "VIEW_CLINICAL"));

        nurse = new AppUserDetails(2L, "nurse.priya", "pass", "NURSE", 1L,
            List.of("ROLE_NURSE"));

        patient = new Patient();
        patient.setPatientId(15L);
        patient.setName("Mohan Das");
        patient.setWardId(2L);

        when(patientRepo.findById(15L)).thenReturn(Optional.of(patient));

        PatientRecord record = new PatientRecord();
        record.setRecordId(101L);
        record.setPatientId(15L);
        record.setType(PatientRecord.RecordType.CLINICAL);
        record.setContent("Critical ICU note");
        when(recordRepo.findByPatientId(15L)).thenReturn(List.of(record));

        AccessLog savedLog = new AccessLog();
        savedLog.setLogId(999L);
        when(accessLogService.logAccess(any(AccessLog.class))).thenReturn(savedLog);
    }

    @Test
    void doctorValidBreakGlass_GrantsAccessAndCreatesMediumAlert() {
        String justification = "Patient in acute respiratory distress, emergency intubation required";
        BreakGlassService.BreakGlassResult result = service.executeBreakGlass(doctor, 15L, justification);

        assertNotNull(result);
        assertEquals(1, result.records().size());
        assertEquals("Critical ICU note", result.records().get(0).getContent());

        // Verify break-glass event saved
        verify(breakGlassRepo).save(any(BreakGlassEvent.class));

        // Verify access log written with BREAK_GLASS action
        verify(accessLogService).logAccess(argThat(log ->
            "BREAK_GLASS".equals(log.getAction()) &&
            log.getUserId().equals(1L) &&
            log.getPatientId().equals(15L)
        ));

        // Verify Medium alert created (score 50)
        verify(alertService).createOrMergeAlert(
            eq(1L), eq("BREAK_GLASS"), eq(50), eq("MEDIUM"),
            contains("Mohan Das"), eq(List.of(999L))
        );
    }

    @Test
    void nonDoctorCannotBreakGlass() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            service.executeBreakGlass(nurse, 15L, "Emergency override attempt by nurse")
        );
        assertTrue(ex.getMessage().contains("Only doctors can use break-glass"));
    }

    @Test
    void justificationTooShort_ThrowsException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            service.executeBreakGlass(doctor, 15L, "too short")
        );
        assertTrue(ex.getMessage().contains("at least 10 characters"));
    }
}
