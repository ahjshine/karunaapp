package com.medsentinel.patient;

import jakarta.persistence.*;

@Entity
@Table(name = "records")
public class PatientRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private RecordType type;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    public enum RecordType { CLINICAL, BILLING, DEMOGRAPHIC }

    public PatientRecord() {}

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public RecordType getType() { return type; }
    public void setType(RecordType type) { this.type = type; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
