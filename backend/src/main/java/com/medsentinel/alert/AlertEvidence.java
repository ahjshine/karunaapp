package com.medsentinel.alert;

import jakarta.persistence.*;

@Entity
@Table(name = "alert_evidence")
@IdClass(AlertEvidenceId.class)
public class AlertEvidence {

    @Id
    @Column(name = "alert_id")
    private Long alertId;

    @Id
    @Column(name = "log_id")
    private Long logId;

    public AlertEvidence() {}

    public AlertEvidence(Long alertId, Long logId) {
        this.alertId = alertId;
        this.logId = logId;
    }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
}
