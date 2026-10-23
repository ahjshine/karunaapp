package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.scoring.ScoringService;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates detection rules. Runs event-driven on each new log entry.
 */
public class DetectionEngine {

    private final List<DetectionRule> rules;
    private final ScoringService scoringService;

    public DetectionEngine(List<DetectionRule> rules, ScoringService scoringService) {
        this.rules = rules;
        this.scoringService = scoringService;
    }

    /**
     * Called after each new log entry is written (event-driven detection).
     */
    public void onNewLogEntry(AccessLog entry) {
        List<RuleHit> allHits = new ArrayList<>();

        for (DetectionRule rule : rules) {
            try {
                List<RuleHit> hits = rule.evaluate(entry);
                if (hits != null && !hits.isEmpty()) {
                    allHits.addAll(hits);
                }
            } catch (Exception e) {
                System.err.println("Rule " + rule.getRuleCode() + " failed on logId=" + entry.getLogId() + ": " + e.getMessage());
            }
        }

        if (!allHits.isEmpty()) {
            scoringService.processHits(allHits);
        }
    }
}
