package com.medsentinel.logging;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AccessLogRepository extends JpaRepository<AccessLog, Long> {

    // Failed logins in a window grouped by username and IP
    @Query("SELECT a FROM AccessLog a WHERE a.action = 'LOGIN' AND a.outcome = 'FAILED' " +
           "AND a.ts >= :since ORDER BY a.ts")
    List<AccessLog> findFailedLoginsSince(@Param("since") LocalDateTime since);

    // All access by a user in a window
    @Query("SELECT a FROM AccessLog a WHERE a.userId = :userId " +
           "AND a.action IN ('VIEW', 'SEARCH', 'EXPORT') AND a.outcome = 'ALLOWED' " +
           "AND a.ts >= :since ORDER BY a.ts")
    List<AccessLog> findAllowedAccessByUserSince(@Param("userId") Long userId,
                                                  @Param("since") LocalDateTime since);

    // Denied access in a window
    @Query("SELECT a FROM AccessLog a WHERE a.outcome = 'DENIED' AND a.ts >= :since ORDER BY a.ts")
    List<AccessLog> findDeniedAccessSince(@Param("since") LocalDateTime since);

    // All logs ordered for chain verification
    @Query("SELECT a FROM AccessLog a ORDER BY a.logId")
    List<AccessLog> findAllOrderedById();

    // Get the last log entry (for hash chaining)
    AccessLog findTopByOrderByLogIdDesc();

    // Logs by user in a time range (for log explorer)
    List<AccessLog> findByUserIdAndTsBetweenOrderByTsDesc(Long userId, LocalDateTime start, LocalDateTime end);

    // Recent logs (for dashboard)
    List<AccessLog> findTop100ByOrderByTsDesc();
    List<AccessLog> findTop20ByOrderByTsDesc();

    // Count failed logins in last 24 hours
    @Query("SELECT COUNT(a) FROM AccessLog a WHERE a.action = 'LOGIN' AND a.outcome = 'FAILED' " +
           "AND a.ts >= :since")
    long countFailedLoginsSince(@Param("since") LocalDateTime since);

    // Login success right after failures for a username
    @Query("SELECT a FROM AccessLog a WHERE a.action = 'LOGIN' AND a.outcome = 'ALLOWED' " +
           "AND a.usernameAttempted = :username AND a.ts >= :since ORDER BY a.ts DESC")
    List<AccessLog> findSuccessfulLoginsSince(@Param("username") String username,
                                              @Param("since") LocalDateTime since);
}
