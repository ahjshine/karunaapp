package com.medsentinel.detection;

import java.util.List;

/**
 * Represents a single rule hit with the affected user, points, explanation,
 * and the log_ids that serve as evidence.
 */
public class RuleHit {

    private final String ruleCode;
    private final Long userId;
    private final String username;
    private final int points;
    private final String explanation;
    private final List<Long> evidenceLogIds;

    public RuleHit(String ruleCode, Long userId, String username,
                   int points, String explanation, List<Long> evidenceLogIds) {
        this.ruleCode = ruleCode;
        this.userId = userId;
        this.username = username;
        this.points = points;
        this.explanation = explanation;
        this.evidenceLogIds = evidenceLogIds;
    }

    public String getRuleCode() { return ruleCode; }
    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public int getPoints() { return points; }
    public String getExplanation() { return explanation; }
    public List<Long> getEvidenceLogIds() { return evidenceLogIds; }
}
