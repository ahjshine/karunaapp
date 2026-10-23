package com.medsentinel.alert;

public class AlertEvidence {

    private Long alertId;
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
