package com.medsentinel.alert;

import com.medsentinel.auth.User;
import com.medsentinel.auth.UserRepository;
import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.logging.LogChainService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class AlertController {

    private final AlertService alertService;
    private final AccessLogRepository accessLogRepo;
    private final LogChainService logChainService;
    private final UserRepository userRepo;

    public AlertController(AlertService alertService,
                           AccessLogRepository accessLogRepo,
                           LogChainService logChainService,
                           UserRepository userRepo) {
        this.alertService = alertService;
        this.accessLogRepo = accessLogRepo;
        this.logChainService = logChainService;
        this.userRepo = userRepo;
    }

    public long countOpenAlerts() { return alertService.countOpenAlerts(); }
    public long countHighSeverity() { return alertService.countHighSeverity(); }
    public long countFailedLogins() {
        return accessLogRepo.countFailedLoginsSince(LocalDateTime.now().minusHours(24));
    }

    public List<Alert> getAlerts() { return alertService.getOpenAlerts(); }
    public List<AccessLog> getRecentLogs() { return accessLogRepo.findTop20ByOrderByTsDesc(); }
    public List<User> getUsers() { return userRepo.findAll(); }

    public Alert getAlert(Long id) { return alertService.getAlert(id); }

    public List<AccessLog> getEvidenceLogs(Long alertId) {
        Alert alert = alertService.getAlert(alertId);
        if (alert == null || alert.getEvidenceLogIds() == null) return List.of();
        return alert.getEvidenceLogIds().stream()
            .map(logId -> accessLogRepo.findById(logId).orElse(null))
            .filter(Objects::nonNull)
            .toList();
    }

    public Alert updateAction(Long id, String action, String note) {
        Alert.AlertStatus status = switch (action.toLowerCase()) {
            case "acknowledge", "ack" -> Alert.AlertStatus.ACK;
            case "escalate", "escalated" -> Alert.AlertStatus.ESCALATED;
            case "false_positive" -> Alert.AlertStatus.FALSE_POSITIVE;
            default -> Alert.AlertStatus.OPEN;
        };
        return alertService.updateStatus(id, status, note);
    }

    public LogChainService.ChainVerificationResult verifyIntegrity() {
        return logChainService.verifyChain();
    }
}
