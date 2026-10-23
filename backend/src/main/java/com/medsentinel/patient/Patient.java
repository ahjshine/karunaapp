package com.medsentinel.patient;

import java.time.LocalDate;

public class Patient {

    private Long patientId;
    private String name;
    private LocalDate dob;
    private Long wardId;

    public Patient() {}

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }
    public Long getWardId() { return wardId; }
    public void setWardId(Long wardId) { this.wardId = wardId; }
}
