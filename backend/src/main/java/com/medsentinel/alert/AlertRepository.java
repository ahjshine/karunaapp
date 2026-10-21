package com.medsentinel.alert;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    // Open alerts ordered by severity then recency
    @Query("SELECT a FROM Alert a WHERE a.status IN ('OPEN', 'ACK', 'ESCALATED') " +
           "ORDER BY CASE a.severity WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, a.lastSeen DESC")
    List<Alert> findOpenAlerts();

    // Find existing alert for dedup (same user, same rules, still open, within window)
    @Query("SELECT a FROM Alert a WHERE a.userId = :userId AND a.ruleCodes = :ruleCodes " +
           "AND a.status = 'OPEN' AND a.lastSeen >= :windowStart")
    Optional<Alert> findForDedup(@Param("userId") Long userId,
                                  @Param("ruleCodes") String ruleCodes,
                                  @Param("windowStart") LocalDateTime windowStart);

    // Alerts by user
    List<Alert> findByUserIdOrderByLastSeenDesc(Long userId);

    // All active alerts for one user in the detection aggregation window.
    @Query("SELECT a FROM Alert a WHERE a.userId = :userId " +
           "AND a.status = 'OPEN' AND a.lastSeen >= :windowStart ORDER BY a.lastSeen DESC")
    List<Alert> findOpenForUserSince(@Param("userId") Long userId,
                                     @Param("windowStart") LocalDateTime windowStart);

    // Count open alerts
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.status IN ('OPEN', 'ACK', 'ESCALATED')")
    long countOpenAlerts();

    // Count high severity open alerts
    @Query("SELECT COUNT(a) FROM Alert a WHERE a.severity = 'HIGH' AND a.status IN ('OPEN', 'ACK', 'ESCALATED')")
    long countHighSeverityOpen();
}
