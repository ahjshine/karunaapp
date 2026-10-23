package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * R3: Out-of-scope access — action/resource outside role permissions.
 * Fires when a DENIED access event is logged.
 */
public class OutOfScopeRule implements DetectionRule {

    private final AccessLogRepository accessLogRepo;

    public OutOfScopeRule() {
        this(new AccessLogRepository());
    }

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
        if (triggeringEntry.getOutcome() != AccessLog.Outcome.DENIED) {
            return Collections.emptyList();
        }

        String username = triggeringEntry.getUsernameAttempted();
        Long userId = triggeringEntry.getUserId();

        LocalDateTime eventTime = triggeringEntry.getTs() != null ? triggeringEntry.getTs() : LocalDateTime.now();
        LocalDateTime since = eventTime.minusMinutes(10);
        List<AccessLog> deniedLogs = accessLogRepo.findDeniedAccessForUserSince(userId, username, since);

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
