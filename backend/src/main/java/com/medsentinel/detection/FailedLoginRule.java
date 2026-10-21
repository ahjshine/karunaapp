package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * R1: Repeated failed logins — ≥5 failures per user or IP in 5 min.
 * R1b: Successful login right after R1 triggers.
 */
@Component
public class FailedLoginRule implements DetectionRule {

    private final AccessLogRepository accessLogRepo;
    private final int threshold;
    private final int windowMinutes;

    public FailedLoginRule(AccessLogRepository accessLogRepo,
                           @Value("${detection.failed-login-threshold:5}") int threshold,
                           @Value("${detection.failed-login-window-minutes:5}") int windowMinutes) {
        this.accessLogRepo = accessLogRepo;
        this.threshold = threshold;
        this.windowMinutes = windowMinutes;
    }

    @Override
    public String getRuleCode() { return "R1"; }

    @Override
    public String getRuleName() { return "Repeated Failed Logins"; }

    @Override
    public int getPoints() { return 30; }

    @Override
    public List<RuleHit> evaluate(AccessLog triggeringEntry) {
        // Only evaluate on LOGIN events
        if (!"LOGIN".equals(triggeringEntry.getAction())) {
            return Collections.emptyList();
        }

        LocalDateTime since = LocalDateTime.now().minusMinutes(windowMinutes);
        List<AccessLog> failures = accessLogRepo.findFailedLoginsSince(since);

        List<RuleHit> hits = new ArrayList<>();

        // Group by username
        Map<String, List<AccessLog>> byUsername = failures.stream()
            .collect(Collectors.groupingBy(AccessLog::getUsernameAttempted));

        for (Map.Entry<String, List<AccessLog>> entry : byUsername.entrySet()) {
            String username = entry.getKey();
            List<AccessLog> userFailures = entry.getValue();

            if (userFailures.size() >= threshold) {
                List<Long> logIds = userFailures.stream()
                    .map(AccessLog::getLogId).toList();

                Long userId = userFailures.stream()
                    .filter(a -> a.getUserId() != null)
                    .map(AccessLog::getUserId)
                    .findFirst().orElse(null);

                hits.add(new RuleHit(
                    "R1", userId, username, 30,
                    String.format("%d failed login attempts for '%s' in the last %d minutes",
                        userFailures.size(), username, windowMinutes),
                    logIds
                ));

                // R1b: Check if there was a successful login after the failures
                if (triggeringEntry.getOutcome() == AccessLog.Outcome.ALLOWED
                        && triggeringEntry.getUsernameAttempted().equals(username)) {
                    List<Long> r1bEvidence = new ArrayList<>(logIds);
                    r1bEvidence.add(triggeringEntry.getLogId());

                    hits.add(new RuleHit(
                        "R1b", userId, username, 25,
                        String.format("Successful login for '%s' immediately after %d failed attempts — possible brute-force success",
                            username, userFailures.size()),
                        r1bEvidence
                    ));
                }
            }
        }

        // Also group by IP
        Map<String, List<AccessLog>> byIp = failures.stream()
            .collect(Collectors.groupingBy(AccessLog::getIp));

        for (Map.Entry<String, List<AccessLog>> entry : byIp.entrySet()) {
            String ip = entry.getKey();
            List<AccessLog> ipFailures = entry.getValue();

            // Only flag if multiple usernames attempted from same IP
            long distinctUsers = ipFailures.stream()
                .map(AccessLog::getUsernameAttempted).distinct().count();

            if (ipFailures.size() >= threshold && distinctUsers > 1) {
                List<Long> logIds = ipFailures.stream()
                    .map(AccessLog::getLogId).toList();

                hits.add(new RuleHit(
                    "R1", null, "IP:" + ip, 30,
                    String.format("%d failed login attempts from IP %s targeting %d different usernames",
                        ipFailures.size(), ip, distinctUsers),
                    logIds
                ));
            }
        }

        return hits;
    }
}
