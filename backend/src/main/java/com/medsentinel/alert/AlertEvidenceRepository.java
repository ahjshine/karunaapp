package com.medsentinel.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlertEvidenceRepository extends JpaRepository<AlertEvidence, AlertEvidenceId> {
    List<AlertEvidence> findByAlertId(Long alertId);
}
