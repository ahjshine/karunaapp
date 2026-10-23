package com.medsentinel.patient;

public class PatientRecord {

    private Long recordId;
    private Long patientId;
    private RecordType type;
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
