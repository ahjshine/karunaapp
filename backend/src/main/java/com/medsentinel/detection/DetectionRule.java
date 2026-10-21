package com.medsentinel.detection;

import com.medsentinel.logging.AccessLog;
import java.util.List;

/**
 * Interface for detection rules. Each rule evaluates recent log data
 * and returns any findings (rule hits with evidence).
 */
public interface DetectionRule {

    /** Unique rule code, e.g. "R1", "R2" */
    String getRuleCode();

    /** Human-readable rule name */
    String getRuleName();

    /** Points added when this rule fires */
    int getPoints();

    /**
     * Evaluate the rule. Called on each new log entry (event-driven)
     * and periodically (scheduled sweep).
     */
    List<RuleHit> evaluate(AccessLog triggeringEntry);
}
