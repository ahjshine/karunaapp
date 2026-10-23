package com.medsentinel.alert;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Creates, deduplicates, and manages alerts.
 */
public class AlertService {

    private final AlertRepository alertRepo;
    private final AlertEvidenceRepository evidenceRepo;

    public AlertService() {
        this(new AlertRepository(), new AlertEvidenceRepository());
    }

    public AlertService(AlertRepository alertRepo, AlertEvidenceRepository evidenceRepo) {
        this.alertRepo = alertRepo;
        this.evidenceRepo = evidenceRepo;
    }

    /**
     * Create a new alert or merge into an existing one (dedup by user + rules + window).
     */
    public Alert createOrMergeAlert(Long userId, String ruleCodes, int score,
                                     String severity, String explanation,
                                     List<Long> evidenceLogIds) {
        if (userId == null) {
            return createNewAlert(null, ruleCodes, score, severity, explanation, evidenceLogIds);
        }

        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(10);
        List<Alert> activeAlerts = alertRepo.findOpenForUserSince(userId, windowStart);

        if (!activeAlerts.isEmpty()) {
            Alert alert = activeAlerts.get(0);
            String mergedRules = mergeRuleCodes(alert.getRuleCodes(), ruleCodes);
            boolean introducesNewRule = !mergedRules.equals(alert.getRuleCodes());
            int mergedScore = introducesNewRule ? Math.max(alert.getScore(), 0) + score
                : Math.max(alert.getScore(), score);
            Alert.Severity mergedSeverity = Alert.Severity.fromScore(mergedScore);
            if (mergedSeverity == null) {
                mergedSeverity = Alert.Severity.valueOf(severity);
            }

            alert.setRuleCodes(mergedRules);
            alert.setScore(mergedScore);
            alert.setSeverity(mergedSeverity);
            alert.setLastSeen(LocalDateTime.now());
            alert.setEventCount(alert.getEventCount() + 1);
            alert.setExplanation(alert.getExplanation() + "; " + explanation);

            if (evidenceLogIds != null) {
                LinkedHashSet<Long> evidence = new LinkedHashSet<>();
                if (alert.getEvidenceLogIds() != null) evidence.addAll(alert.getEvidenceLogIds());
                evidence.addAll(evidenceLogIds);
                alert.setEvidenceLogIds(new ArrayList<>(evidence));
            }

            alertRepo.save(alert);
            addEvidence(alert.getAlertId(), evidenceLogIds);
            return alert;
        }

        return createNewAlert(userId, ruleCodes, score, severity, explanation, evidenceLogIds);
    }

    private Alert createNewAlert(Long userId, String ruleCodes, int score,
                                  String severity, String explanation,
                                  List<Long> evidenceLogIds) {
        Alert alert = new Alert();
        alert.setUserId(userId);
        alert.setRuleCodes(ruleCodes);
        alert.setScore(score);
        alert.setSeverity(Alert.Severity.valueOf(severity));
        alert.setExplanation(explanation);
        if (evidenceLogIds != null) {
            alert.setEvidenceLogIds(new ArrayList<>(evidenceLogIds));
        }
        alert = alertRepo.save(alert);

        addEvidence(alert.getAlertId(), evidenceLogIds);
        return alert;
    }

    private String mergeRuleCodes(String existing, String incoming) {
        return java.util.stream.Stream.concat(
                java.util.Arrays.stream(existing.split(",")),
                java.util.Arrays.stream(incoming.split(",")))
            .filter(code -> !code.isBlank())
            .distinct()
            .sorted()
            .collect(java.util.stream.Collectors.joining(","));
    }

    private void addEvidence(Long alertId, List<Long> logIds) {
        if (logIds == null) return;
        for (Long logId : logIds) {
            AlertEvidenceId id = new AlertEvidenceId(alertId, logId);
            if (!evidenceRepo.existsById(id)) {
                evidenceRepo.save(new AlertEvidence(alertId, logId));
            }
        }
    }

    public Alert updateStatus(Long alertId, Alert.AlertStatus newStatus, String note) {
        Alert alert = alertRepo.findById(alertId)
            .orElseThrow(() -> new RuntimeException("Alert not found: " + alertId));
        alert.setStatus(newStatus);
        if (note != null && !note.isBlank()) {
            alert.setReviewerNote(note);
        }
        return alertRepo.save(alert);
    }

    public List<Alert> getOpenAlerts() {
        return alertRepo.findOpenAlerts();
    }

    public Alert getAlert(Long alertId) {
        return alertRepo.findById(alertId).orElse(null);
    }

    public List<AlertEvidence> getEvidence(Long alertId) {
        return evidenceRepo.findByAlertId(alertId);
    }

    public long countOpenAlerts() {
        return alertRepo.countOpenAlerts();
    }

    public long countHighSeverity() {
        return alertRepo.countHighSeverityOpen();
    }
}
