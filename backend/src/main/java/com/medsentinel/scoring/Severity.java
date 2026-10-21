package com.medsentinel.scoring;

/**
 * Severity levels derived from risk score.
 */
public enum Severity {
    LOW,    // 30-49
    MEDIUM, // 50-69
    HIGH;   // >= 70

    public static Severity fromScore(int score) {
        if (score >= 70) return HIGH;
        if (score >= 50) return MEDIUM;
        if (score >= 30) return LOW;
        return null; // Below threshold
    }
}
