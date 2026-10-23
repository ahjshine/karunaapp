package com.medsentinel.patient;

public class CareAssignment {

    private Long userId;
    private Long patientId;

    public CareAssignment() {}

    public CareAssignment(Long userId, Long patientId) {
        this.userId = userId;
        this.patientId = patientId;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
}
