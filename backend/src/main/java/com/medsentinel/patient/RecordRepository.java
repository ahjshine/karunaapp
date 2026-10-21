package com.medsentinel.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecordRepository extends JpaRepository<PatientRecord, Long> {
    List<PatientRecord> findByPatientId(Long patientId);
    List<PatientRecord> findByPatientIdAndType(Long patientId, PatientRecord.RecordType type);
}
