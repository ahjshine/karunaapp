package com.medsentinel.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Creates, deduplicates, and manages alerts.
 */
@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);
    private final AlertRepository alertRepo;
    private final AlertEvidenceRepository evidenceRepo;

    public AlertService(AlertRepository alertRepo, AlertEvidenceRepository evidenceRepo) {
        this.alertRepo = alertRepo;
        this.evidenceRepo = evidenceRepo;
    }

    /**
     * Create a new alert or merge into an existing one (dedup by user + rules + window).
     */
    @Transactional
    public Alert createOrMergeAlert(Long userId, String ruleCodes, int score,
                                     String severity, String explanation,
                                     List<Long> evidenceLogIds) {
        if (userId == null) {
            // Can't dedup without a userId; create a new alert with a placeholder
            return createNewAlert(null, ruleCodes, score, severity, explanation, evidenceLogIds);
        }

        // Merge all rule hits for a user during one ten-minute correlation window.
        // This lets a clerk's R3 denial and subsequent R2 bulk pull become one High alert.
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
            alertRepo.save(alert);
            addEvidence(alert.getAlertId(), evidenceLogIds);

            log.info("Merged into existing alert A-{}: rules={}, score={}, count={}",
                alert.getAlertId(), mergedRules, mergedScore, alert.getEventCount());
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
        alert = alertRepo.save(alert);

        addEvidence(alert.getAlertId(), evidenceLogIds);

        log.info("Created new alert A-{}: rules={}, severity={}", alert.getAlertId(), ruleCodes, severity);
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
        for (Long logId : logIds) {
            AlertEvidenceId id = new AlertEvidenceId(alertId, logId);
            if (!evidenceRepo.existsById(id)) {
                evidenceRepo.save(new AlertEvidence(alertId, logId));
            }
        }
    }

    /**
     * Update alert status (reviewer action).
     */
    @Transactional
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
