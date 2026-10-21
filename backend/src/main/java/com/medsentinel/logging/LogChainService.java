package com.medsentinel.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Manages the SHA-256 hash chain on access_log rows.
 * Each row's hash = SHA-256(prev_hash + canonical_row_string).
 */
@Service
public class LogChainService {

    private static final Logger log = LoggerFactory.getLogger(LogChainService.class);
    private final AccessLogRepository accessLogRepo;

    public LogChainService(AccessLogRepository accessLogRepo) {
        this.accessLogRepo = accessLogRepo;
    }

    /**
     * Compute and store the hash for a newly inserted log entry.
     * The caller resolves the preceding hash before inserting the new row.
     */
    @Transactional
    public void chainHash(AccessLog entry, String previousHash) {
        String prevHash = previousHash != null ? previousHash : "0".repeat(64);
        entry.setPrevHash(prevHash);
        entry.setRowHash(sha256(prevHash + "|" + entry.toCanonicalString()));
        accessLogRepo.save(entry);
    }

    /**
     * Verify the entire hash chain. Returns the first broken log_id, or null if intact.
     */
    public ChainVerificationResult verifyChain() {
        List<AccessLog> allLogs = accessLogRepo.findAllOrderedById();

        if (allLogs.isEmpty()) {
            return new ChainVerificationResult(true, null, 0, 0);
        }

        String prevHash = "0".repeat(64);
        int checked = 0;

        for (AccessLog entry : allLogs) {
            checked++;

            // Check prev_hash linkage
            if (entry.getPrevHash() != null && !entry.getPrevHash().equals(prevHash)) {
                return new ChainVerificationResult(false, entry.getLogId(), checked, allLogs.size());
            }

            // Verify row_hash
            if (entry.getRowHash() != null) {
                String canonical = entry.getPrevHash() + "|" + entry.toCanonicalString();
                String expectedHash = sha256(canonical);
                if (!expectedHash.equals(entry.getRowHash())) {
                    return new ChainVerificationResult(false, entry.getLogId(), checked, allLogs.size());
                }
            }

            prevHash = entry.getRowHash() != null ? entry.getRowHash() : prevHash;
        }

        return new ChainVerificationResult(true, null, checked, allLogs.size());
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Result of chain verification.
     */
    public record ChainVerificationResult(
        boolean intact,
        Long brokenAtLogId,
        int rowsChecked,
        int totalRows
    ) {}
}
