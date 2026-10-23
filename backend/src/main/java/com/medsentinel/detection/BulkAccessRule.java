package com.medsentinel.detection;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.rbac.AccessGuard;
import com.medsentinel.rbac.RoleBaseline;
import com.medsentinel.rbac.RoleBaselineRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * R2: Unusually large retrieval — records read in 10 min > role baseline × multiplier.
 * Includes busy-clinician guard: on-ward/care-team access uses a higher baseline.
 */
public class BulkAccessRule implements DetectionRule {

    private final AccessLogRepository accessLogRepo;
    private final UserRepository userRepo;
    private final RoleBaselineRepository baselineRepo;
    private final AccessGuard accessGuard;
    private final int windowMinutes;

    public BulkAccessRule(AccessLogRepository accessLogRepo,
                          UserRepository userRepo,
                          RoleBaselineRepository baselineRepo,
                          AccessGuard accessGuard) {
        this(accessLogRepo, userRepo, baselineRepo, accessGuard, 10);
    }

    public BulkAccessRule(AccessLogRepository accessLogRepo,
                          UserRepository userRepo,
                          RoleBaselineRepository baselineRepo,
                          AccessGuard accessGuard,
                          int windowMinutes) {
        this.accessLogRepo = accessLogRepo;
        this.userRepo = userRepo;
        this.baselineRepo = baselineRepo;
        this.accessGuard = accessGuard;
        this.windowMinutes = windowMinutes;
    }

    @Override
    public String getRuleCode() { return "R2"; }

    @Override
    public String getRuleName() { return "Bulk Record Access"; }

    @Override
    public int getPoints() { return 35; }

    @Override
    public List<RuleHit> evaluate(AccessLog triggeringEntry) {
        if (triggeringEntry.getOutcome() != AccessLog.Outcome.ALLOWED) {
            return Collections.emptyList();
        }
        if (!Set.of("VIEW", "SEARCH", "EXPORT").contains(triggeringEntry.getAction())) {
            return Collections.emptyList();
        }
        if (triggeringEntry.getUserId() == null) {
            return Collections.emptyList();
        }

        Long userId = triggeringEntry.getUserId();
        User user = userRepo.findById(userId).orElse(null);
        if (user == null || user.getRole() == null) return Collections.emptyList();

        LocalDateTime eventTime = triggeringEntry.getTs() != null ? triggeringEntry.getTs() : LocalDateTime.now();
        LocalDateTime since = eventTime.minusMinutes(windowMinutes);
        List<AccessLog> recentAccess = accessLogRepo.findAllowedAccessByUserSince(userId, since);

        if (recentAccess.isEmpty()) return Collections.emptyList();

        int onWardCount = 0;
        int offWardCount = 0;
        List<Long> allLogIds = new ArrayList<>();

        for (AccessLog log : recentAccess) {
            allLogIds.add(log.getLogId());
            if (log.getPatientId() != null &&
                    accessGuard.isOnWardOrAssigned(userId, log.getPatientId(), user.getWardId())) {
                onWardCount++;
            } else {
                offWardCount++;
            }
        }

        List<RuleHit> hits = new ArrayList<>();
        Long roleId = user.getRole().getRoleId();

        // Check on-ward baseline
        RoleBaseline onWardBaseline = baselineRepo.findByRoleIdAndScope(roleId, RoleBaseline.BaselineScope.ON_WARD);
        if (onWardBaseline != null && onWardCount > 0) {
            int threshold = onWardBaseline.getMultiplier()
                .multiply(BigDecimal.valueOf(onWardBaseline.getMaxRecordsPer10Min()))
                .intValue();
            if (onWardCount > threshold) {
                hits.add(new RuleHit("R2", userId, user.getUsername(), 35,
                    String.format("%s (%s) accessed %d on-ward records in %d min (baseline: %d, threshold: %d)",
                        user.getUsername(), user.getRole().getName(),
                        onWardCount, windowMinutes,
                        onWardBaseline.getMaxRecordsPer10Min(), threshold),
                    allLogIds));
            }
        }

        // Check off-ward baseline
        RoleBaseline offWardBaseline = baselineRepo.findByRoleIdAndScope(roleId, RoleBaseline.BaselineScope.OFF_WARD);
        if (offWardBaseline != null && offWardCount > 0) {
            int threshold = offWardBaseline.getMultiplier()
                .multiply(BigDecimal.valueOf(offWardBaseline.getMaxRecordsPer10Min()))
                .intValue();
            if (offWardCount > threshold) {
                hits.add(new RuleHit("R2", userId, user.getUsername(), 35,
                    String.format("%s (%s) accessed %d off-ward records in %d min (baseline: %d, threshold: %d)",
                        user.getUsername(), user.getRole().getName(),
                        offWardCount, windowMinutes,
                        offWardBaseline.getMaxRecordsPer10Min(), threshold),
                    allLogIds));
            }
        }

        // Check ANY baseline (total volume)
        RoleBaseline anyBaseline = baselineRepo.findByRoleIdAndScope(roleId, RoleBaseline.BaselineScope.ANY);
        if (anyBaseline != null) {
            int total = recentAccess.size();
            int threshold = anyBaseline.getMultiplier()
                .multiply(BigDecimal.valueOf(anyBaseline.getMaxRecordsPer10Min()))
                .intValue();
            if (total > threshold) {
                hits.add(new RuleHit("R2", userId, user.getUsername(), 35,
                    String.format("%s (%s) accessed %d total records in %d min (baseline: %d, threshold: %d)",
                        user.getUsername(), user.getRole().getName(),
                        total, windowMinutes,
                        anyBaseline.getMaxRecordsPer10Min(), threshold),
                    allLogIds));
            }
        }

        return hits;
    }
}
