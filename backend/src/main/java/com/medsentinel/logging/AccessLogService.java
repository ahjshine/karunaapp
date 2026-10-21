package com.medsentinel.logging;

import com.medsentinel.detection.DetectionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Central service for writing access log entries.
 * Handles hash chaining and triggers detection asynchronously.
 */
@Service
public class AccessLogService {

    private static final Logger log = LoggerFactory.getLogger(AccessLogService.class);
    private final AccessLogRepository accessLogRepo;
    private final LogChainService logChainService;
    private final ApplicationEventPublisher eventPublisher;

    public AccessLogService(AccessLogRepository accessLogRepo,
                            LogChainService logChainService,
                            ApplicationEventPublisher eventPublisher) {
        this.accessLogRepo = accessLogRepo;
        this.logChainService = logChainService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Log an access event, chain its hash, and trigger detection.
     */
    @Transactional
    public AccessLog logAccess(AccessLog entry) {
        // Resolve the preceding row before saving so the new entry is not its own predecessor.
        AccessLog previousEntry = accessLogRepo.findTopByOrderByLogIdDesc();
        String previousHash = previousEntry != null ? previousEntry.getRowHash() : null;

        // Save first to allocate the log_id used in the canonical hash representation.
        AccessLog saved = accessLogRepo.save(entry);

        // Compute and persist the hash chain.
        logChainService.chainHash(saved, previousHash);

        log.debug("Logged: action={}, user={}, outcome={}, logId={}",
            saved.getAction(), saved.getUsernameAttempted(),
            saved.getOutcome(), saved.getLogId());

        // Publish access log event for detection and auditing
        try {
            eventPublisher.publishEvent(saved);
        } catch (Exception e) {
            log.error("Detection event failed for logId={}", saved.getLogId(), e);
        }

        return saved;
    }
}
