package com.medsentinel.breakglass;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "break_glass_events")
public class BreakGlassEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bg_id")
    private Long bgId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "justification", nullable = false)
    private String justification;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "reviewed", nullable = false)
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
