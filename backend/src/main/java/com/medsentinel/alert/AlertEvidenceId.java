package com.medsentinel.alert;

import java.io.Serializable;
import java.util.Objects;

public class AlertEvidenceId implements Serializable {

    private Long alertId;
    private Long logId;

    public AlertEvidenceId() {}

    public AlertEvidenceId(Long alertId, Long logId) {
        this.alertId = alertId;
        this.logId = logId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertEvidenceId that)) return false;
        return Objects.equals(alertId, that.alertId) && Objects.equals(logId, that.logId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId, logId);
    }
}
