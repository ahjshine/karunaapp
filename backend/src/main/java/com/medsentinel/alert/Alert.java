package com.medsentinel.alert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Alert {

    private Long alertId;
    private Long userId;
    private String ruleCodes;
    private Integer score;
    private Severity severity;
    private String explanation;
    private AlertStatus status = AlertStatus.OPEN;
    private String reviewerNote;
    private LocalDateTime firstSeen;
    private LocalDateTime lastSeen;
    private Integer eventCount = 1;
    private List<Long> evidenceLogIds = new ArrayList<>();

    public enum Severity {
        LOW, MEDIUM, HIGH;

        public static Severity fromScore(int score) {
            if (score >= 70) return HIGH;
            if (score >= 50) return MEDIUM;
            if (score >= 30) return LOW;
            return null; // Below threshold, no alert
        }
    }

    public enum AlertStatus { OPEN, ACK, ESCALATED, FALSE_POSITIVE }

    public Alert() {
        this.firstSeen = LocalDateTime.now();
        this.lastSeen = LocalDateTime.now();
    }

    public Long getAlertId() { return alertId; }
    public void setAlertId(Long alertId) { this.alertId = alertId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getRuleCodes() { return ruleCodes; }
    public void setRuleCodes(String ruleCodes) { this.ruleCodes = ruleCodes; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public AlertStatus getStatus() { return status; }
    public void setStatus(AlertStatus status) { this.status = status; }
    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
    public LocalDateTime getFirstSeen() { return firstSeen; }
    public void setFirstSeen(LocalDateTime firstSeen) { this.firstSeen = firstSeen; }
    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }
    public Integer getEventCount() { return eventCount; }
    public void setEventCount(Integer eventCount) { this.eventCount = eventCount; }
    public List<Long> getEvidenceLogIds() { return evidenceLogIds; }
    public void setEvidenceLogIds(List<Long> evidenceLogIds) { this.evidenceLogIds = evidenceLogIds; }
}
