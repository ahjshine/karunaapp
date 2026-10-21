package com.medsentinel.alert;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogRepository;
import com.medsentinel.logging.LogChainService;
import com.medsentinel.auth.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard")
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

    @GetMapping
    public String dashboard(Model model) {
        // KPI cards
        model.addAttribute("openAlerts", alertService.countOpenAlerts());
        model.addAttribute("highSeverity", alertService.countHighSeverity());
        model.addAttribute("failedLogins", accessLogRepo.countFailedLoginsSince(
            LocalDateTime.now().minusHours(24)));

        // Alert feed
        model.addAttribute("alerts", alertService.getOpenAlerts());

        // Recent logs
        model.addAttribute("recentLogs", accessLogRepo.findTop20ByOrderByTsDesc());

        // Users for reference
        model.addAttribute("users", userRepo.findAll());

        return "dashboard/index";
    }

    @GetMapping("/alerts/{id}")
    public String alertDetail(@PathVariable Long id, Model model) {
        Alert alert = alertService.getAlert(id);
        if (alert == null) return "redirect:/dashboard";

        model.addAttribute("alert", alert);

        // Get evidence log rows
        List<AlertEvidence> evidence = alertService.getEvidence(id);
        List<Long> logIds = evidence.stream().map(AlertEvidence::getLogId).toList();
        List<AccessLog> evidenceLogs = logIds.stream()
            .map(logId -> accessLogRepo.findById(logId).orElse(null))
            .filter(l -> l != null)
            .toList();
        model.addAttribute("evidenceLogs", evidenceLogs);

        // Get user info
        if (alert.getUserId() != null && alert.getUserId() > 0) {
            userRepo.findById(alert.getUserId()).ifPresent(u -> model.addAttribute("alertUser", u));
        }

        return "dashboard/alert-detail";
    }

    @PostMapping("/alerts/{id}/action")
    public String alertAction(@PathVariable Long id,
                              @RequestParam String action,
                              @RequestParam(required = false) String note) {
        Alert.AlertStatus status = switch (action) {
            case "acknowledge" -> Alert.AlertStatus.ACK;
            case "escalate" -> Alert.AlertStatus.ESCALATED;
            case "false_positive" -> Alert.AlertStatus.FALSE_POSITIVE;
            default -> throw new IllegalArgumentException("Unknown action: " + action);
        };
        alertService.updateStatus(id, status, note);
        return "redirect:/dashboard";
    }

    @GetMapping("/logs")
    public String logExplorer(Model model,
                              @RequestParam(required = false) Long userId,
                              @RequestParam(required = false) String outcome,
                              @RequestParam(required = false) String from,
                              @RequestParam(required = false) String to) {
        List<AccessLog> logs;
        LocalDateTime start = from != null ? LocalDateTime.parse(from) : LocalDateTime.now().minusDays(1);
        LocalDateTime end = to != null ? LocalDateTime.parse(to) : LocalDateTime.now();

        if (userId != null) {
            logs = accessLogRepo.findByUserIdAndTsBetweenOrderByTsDesc(userId, start, end);
        } else {
            logs = accessLogRepo.findTop100ByOrderByTsDesc();
        }

        model.addAttribute("logs", logs);
        model.addAttribute("users", userRepo.findAll());
        return "dashboard/logs";
    }

    @GetMapping("/integrity")
    public String integrityCheck(Model model) {
        LogChainService.ChainVerificationResult result = logChainService.verifyChain();
        model.addAttribute("integrityResult", result);
        return "dashboard/integrity";
    }
}
