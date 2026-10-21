package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * R3: Out-of-scope access — action/resource outside role permissions.
 * Fires when a DENIED access event is logged.
 */
@Component
public class OutOfScopeRule implements DetectionRule {

    private final AccessLogRepository accessLogRepo;

    public OutOfScopeRule(AccessLogRepository accessLogRepo) {
        this.accessLogRepo = accessLogRepo;
    }

    @Override
    public String getRuleCode() { return "R3"; }

    @Override
    public String getRuleName() { return "Out-of-Scope Access"; }

    @Override
    public int getPoints() { return 40; }

    @Override
    public List<RuleHit> evaluate(AccessLog triggeringEntry) {
        // Only fires on DENIED events
        if (triggeringEntry.getOutcome() != AccessLog.Outcome.DENIED) {
            return Collections.emptyList();
        }

        // Each denied access is itself evidence
        String username = triggeringEntry.getUsernameAttempted();
        Long userId = triggeringEntry.getUserId();

        // Collect all denied events in the window for this user
        LocalDateTime since = LocalDateTime.now().minusMinutes(10);
        List<AccessLog> deniedLogs = accessLogRepo.findDeniedAccessSince(since).stream()
            .filter(a -> {
                if (userId != null && userId.equals(a.getUserId())) return true;
                return username.equals(a.getUsernameAttempted());
            })
            .toList();

        if (deniedLogs.isEmpty()) return Collections.emptyList();

        List<Long> logIds = deniedLogs.stream().map(AccessLog::getLogId).toList();

        return List.of(new RuleHit(
            "R3", userId, username, 40,
            String.format("%s attempted to access %s (%s) without permission: %s",
                username,
                triggeringEntry.getResourceType() != null ? triggeringEntry.getResourceType() : "resource",
                triggeringEntry.getAction(),
                triggeringEntry.getReason()),
            logIds
        ));
    }
}
