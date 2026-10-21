package com.medsentinel.logging;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_log")
public class AccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "ts", nullable = false)
    private LocalDateTime ts;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username_attempted", nullable = false)
    private String usernameAttempted;

    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "patient_id")
    private Long patientId;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false)
    private Outcome outcome;

    @Column(name = "reason")
    private String reason;

    @Column(name = "prev_hash")
    private String prevHash;

    @Column(name = "row_hash")
    private String rowHash;

    public enum Outcome { ALLOWED, DENIED, FAILED }

    public AccessLog() {
        this.ts = LocalDateTime.now();
        this.ip = "127.0.0.1";
    }

    // Builder-style setters for fluent construction
    public AccessLog userId(Long userId) { this.userId = userId; return this; }
    public AccessLog usernameAttempted(String u) { this.usernameAttempted = u; return this; }
    public AccessLog ip(String ip) { this.ip = ip; return this; }
    public AccessLog action(String action) { this.action = action; return this; }
    public AccessLog resourceType(String rt) { this.resourceType = rt; return this; }
    public AccessLog resourceId(Long rid) { this.resourceId = rid; return this; }
    public AccessLog patientId(Long pid) { this.patientId = pid; return this; }
    public AccessLog outcome(Outcome outcome) { this.outcome = outcome; return this; }
    public AccessLog reason(String reason) { this.reason = reason; return this; }

    // Standard getters/setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsernameAttempted() { return usernameAttempted; }
    public void setUsernameAttempted(String usernameAttempted) { this.usernameAttempted = usernameAttempted; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }
    public Long getResourceId() { return resourceId; }
    public void setResourceId(Long resourceId) { this.resourceId = resourceId; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }
    public String getRowHash() { return rowHash; }
    public void setRowHash(String rowHash) { this.rowHash = rowHash; }

    /**
     * Returns a canonical string of this row's fields for hash computation.
     * Excludes prev_hash and row_hash themselves.
     */
    public String toCanonicalString() {
        return String.join("|",
            String.valueOf(logId),
            ts != null ? ts.toString() : "",
            userId != null ? userId.toString() : "",
            usernameAttempted != null ? usernameAttempted : "",
            ip != null ? ip : "",
            action != null ? action : "",
            resourceType != null ? resourceType : "",
            resourceId != null ? resourceId.toString() : "",
            patientId != null ? patientId.toString() : "",
            outcome != null ? outcome.name() : "",
            reason != null ? reason : ""
        );
    }
}
