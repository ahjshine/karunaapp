package com.medsentinel.patient;

import jakarta.persistence.*;

@Entity
@Table(name = "care_assignments")
@IdClass(CareAssignmentId.class)
public class CareAssignment {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "patient_id")
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
