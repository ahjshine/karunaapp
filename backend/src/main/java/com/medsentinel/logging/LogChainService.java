package com.medsentinel.logging;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Manages the SHA-256 hash chain on access_log rows.
 * Each row's hash = SHA-256(prev_hash + canonical_row_string).
 */
public class LogChainService {

    private static final String GENESIS_HASH = "0".repeat(64);
    private final AccessLogRepository accessLogRepo;

    public LogChainService() {
        this(new AccessLogRepository());
    }

    public LogChainService(AccessLogRepository accessLogRepo) {
        this.accessLogRepo = accessLogRepo;
    }

    /**
     * Append a log entry using the latest row as its predecessor.
     * The synchronized method keeps concurrent writers in this JVM ordered.
     */
    public synchronized AccessLog append(AccessLog entry) {
        if (entry == null) {
            throw new IllegalArgumentException("Access log entry is required");
        }

        AccessLog previousEntry = accessLogRepo.findTopByOrderByLogIdDesc();
        String previousHash = previousEntry != null ? previousEntry.getRowHash() : GENESIS_HASH;
        if (previousEntry != null && (previousHash == null || previousHash.isBlank())) {
            throw new IllegalStateException("Cannot append to an incomplete hash chain");
        }

        return append(entry, previousHash);
    }

    public synchronized AccessLog append(AccessLog entry, String previousHash) {
        if (entry == null) {
            throw new IllegalArgumentException("Access log entry is required");
        }
        if (entry.getLogId() == null) {
            entry.setLogId(accessLogRepo.nextId());
        }

        applyHash(entry, previousHash);
        return accessLogRepo.append(entry);
    }

    /**
     * Compute and store a hash when the caller already has the predecessor hash.
     * This is useful for sequential bulk imports such as the demo data seeder.
     */
    public synchronized void chainHash(AccessLog entry, String previousHash) {
        append(entry, previousHash);
    }

    private void applyHash(AccessLog entry, String previousHash) {
        String prevHash = previousHash != null ? previousHash : GENESIS_HASH;
        entry.setPrevHash(prevHash);
        entry.setRowHash(sha256(prevHash + "|" + entry.toCanonicalString()));
    }

    /**
     * Verify the entire hash chain. Returns the first broken log_id, or null if intact.
     */
    public ChainVerificationResult verifyChain() {
        List<AccessLog> allLogs = accessLogRepo.findAllOrderedById();

        if (allLogs.isEmpty()) {
            return new ChainVerificationResult(true, null, 0, 0);
        }

        String prevHash = GENESIS_HASH;
        int checked = 0;

        for (AccessLog entry : allLogs) {
            checked++;

            // Check prev_hash linkage
            if (!prevHash.equals(entry.getPrevHash())) {
                return new ChainVerificationResult(false, entry.getLogId(), checked, allLogs.size());
            }

            // Verify row_hash
            if (entry.getRowHash() == null || entry.getRowHash().isBlank()) {
                return new ChainVerificationResult(false, entry.getLogId(), checked, allLogs.size());
            }

            String canonical = entry.getPrevHash() + "|" + entry.toCanonicalString();
            String expectedHash = sha256(canonical);
            if (!expectedHash.equals(entry.getRowHash())) {
                return new ChainVerificationResult(false, entry.getLogId(), checked, allLogs.size());
            }

            prevHash = entry.getRowHash();
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
