package com.medsentinel.scoring;

import com.medsentinel.alert.AlertService;
import com.medsentinel.detection.RuleHit;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Aggregates rule hits per user window, computes total score,
 * maps to severity, and hands off to AlertService.
 */
public class ScoringService {

    private final AlertService alertService;

    public ScoringService(AlertService alertService) {
        this.alertService = alertService;
    }

    /**
     * Process a batch of rule hits. Groups by userId, sums points,
     * determines severity, and creates/merges alerts.
     */
    public void processHits(List<RuleHit> hits) {
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
        int totalScore = hits.stream().mapToInt(RuleHit::getPoints).sum();

        Severity severity = Severity.fromScore(totalScore);
        if (severity == null) {
            return;
        }

        String ruleCodes = hits.stream()
            .map(RuleHit::getRuleCode)
            .distinct()
            .sorted()
            .collect(Collectors.joining(","));

        String explanation = hits.stream()
            .map(RuleHit::getExplanation)
            .collect(Collectors.joining("; "));

        List<Long> allEvidence = hits.stream()
            .flatMap(h -> h.getEvidenceLogIds().stream())
            .distinct()
            .toList();

        Long userId = hits.stream()
            .filter(h -> h.getUserId() != null)
            .map(RuleHit::getUserId)
            .findFirst().orElse(null);

        alertService.createOrMergeAlert(userId, ruleCodes, totalScore,
            severity.name(), explanation, allEvidence);
    }
}
