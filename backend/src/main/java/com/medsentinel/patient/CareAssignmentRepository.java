package com.medsentinel.patient;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CareAssignmentRepository extends JpaRepository<CareAssignment, CareAssignmentId> {
    boolean existsByUserIdAndPatientId(Long userId, Long patientId);
    java.util.List<CareAssignment> findByUserId(Long userId);
}
