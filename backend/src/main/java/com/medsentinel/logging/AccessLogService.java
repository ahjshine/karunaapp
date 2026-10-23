package com.medsentinel.logging;

import com.medsentinel.detection.DetectionEngine;

/**
 * Central service for writing access log entries.
 * Handles hash chaining and triggers detection.
 */
public class AccessLogService {

    private final AccessLogRepository accessLogRepo;
    private final LogChainService logChainService;
    private DetectionEngine detectionEngine;

    public AccessLogService(AccessLogRepository accessLogRepo, LogChainService logChainService) {
        this.accessLogRepo = accessLogRepo;
        this.logChainService = logChainService;
    }

    public void setDetectionEngine(DetectionEngine detectionEngine) {
        this.detectionEngine = detectionEngine;
    }

    /**
     * Log an access event, chain its hash, and trigger detection.
     */
    public synchronized AccessLog logAccess(AccessLog entry) {
        AccessLog saved = logChainService.append(entry);

        // Trigger detection engine
        if (detectionEngine != null) {
            try {
                detectionEngine.onNewLogEntry(saved);
            } catch (Exception e) {
                System.err.println("Detection failed for logId=" + saved.getLogId() + ": " + e.getMessage());
            }
        }

        return saved;
    }
}
