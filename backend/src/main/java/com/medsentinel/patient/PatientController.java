package com.medsentinel.patient;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.breakglass.BreakGlassService;

import java.util.List;

public class PatientController {

    private final PatientService patientService;
    private final BreakGlassService breakGlassService;

    public PatientController(PatientService patientService, BreakGlassService breakGlassService) {
        this.patientService = patientService;
        this.breakGlassService = breakGlassService;
    }

    public List<Patient> listPatients() {
        return patientService.listPatients();
    }

    public Patient getPatient(Long id) {
        return patientService.getPatient(id);
    }

    public PatientService.PatientRecordBundle getAccessibleRecords(AppUserDetails user, Long id) {
        return patientService.getAccessibleRecords(user, id);
    }

    public BreakGlassService.BreakGlassResult breakGlass(AppUserDetails user, Long id, String justification) {
        return breakGlassService.executeBreakGlass(user, id, justification);
    }

    public List<Patient> search(AppUserDetails user, String query) {
        return patientService.searchPatients(user, query);
    }
}
