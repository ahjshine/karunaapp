package com.medsentinel.patient;

import java.io.Serializable;
import java.util.Objects;

public class CareAssignmentId implements Serializable {

    private Long userId;
    private Long patientId;

    public CareAssignmentId() {}

    public CareAssignmentId(Long userId, Long patientId) {
        this.userId = userId;
        this.patientId = patientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CareAssignmentId that)) return false;
        return Objects.equals(userId, that.userId) && Objects.equals(patientId, that.patientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, patientId);
    }
}
