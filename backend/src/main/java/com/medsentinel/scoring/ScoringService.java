package com.medsentinel.scoring;

import com.medsentinel.alert.AlertService;
import com.medsentinel.detection.RuleHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates rule hits per user window, computes total score,
 * maps to severity, and hands off to AlertService.
 */
@Service
public class ScoringService {

    private static final Logger log = LoggerFactory.getLogger(ScoringService.class);
    private final AlertService alertService;

    public ScoringService(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Process a batch of rule hits. Groups by userId, sums points,
     * determines severity, and creates/merges alerts.
     */
    public void processHits(List<RuleHit> hits) {
        // Group hits by userId (or username for anonymous)
        Map<String, List<RuleHit>> grouped = hits.stream()
            .collect(Collectors.groupingBy(h ->
                h.getUserId() != null ? "uid:" + h.getUserId() : "user:" + h.getUsername()
            ));

        for (Map.Entry<String, List<RuleHit>> entry : grouped.entrySet()) {
            List<RuleHit> userHits = entry.getValue();
            processUserHits(userHits);
        }
    }

    private void processUserHits(List<RuleHit> hits) {
        // Sum points
        int totalScore = hits.stream().mapToInt(RuleHit::getPoints).sum();

        // Determine severity
        Severity severity = Severity.fromScore(totalScore);
        if (severity == null) {
            log.debug("Score {} below threshold, no alert for user {}",
                totalScore, hits.get(0).getUsername());
            return;
        }

        // Collect rule codes
        String ruleCodes = hits.stream()
            .map(RuleHit::getRuleCode)
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));

        // Build explanation
        String explanation = hits.stream()
            .map(RuleHit::getExplanation)
            .collect(Collectors.joining("; "));

        // Collect all evidence log IDs
        List<Long> allEvidence = hits.stream()
            .flatMap(h -> h.getEvidenceLogIds().stream())
            .distinct()
            .toList();

        Long userId = hits.stream()
            .filter(h -> h.getUserId() != null)
            .map(RuleHit::getUserId)
            .findFirst().orElse(null);

        log.info("Alert: user={}, rules={}, score={}, severity={}",
            hits.get(0).getUsername(), ruleCodes, totalScore, severity);

        alertService.createOrMergeAlert(userId, ruleCodes, totalScore,
            severity.name(), explanation, allEvidence);
    }
}
