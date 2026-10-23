package com.medsentinel.breakglass;

import java.time.LocalDateTime;

public class BreakGlassEvent {

    private Long bgId;
    private Long userId;
    private Long patientId;
    private String justification;
    private LocalDateTime ts;
    private Boolean reviewed = false;

    public BreakGlassEvent() {
        this.ts = LocalDateTime.now();
    }

    public Long getBgId() { return bgId; }
    public void setBgId(Long bgId) { this.bgId = bgId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }
    public Boolean getReviewed() { return reviewed; }
    public void setReviewed(Boolean reviewed) { this.reviewed = reviewed; }
}
