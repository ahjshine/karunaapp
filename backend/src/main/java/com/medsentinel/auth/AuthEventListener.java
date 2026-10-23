package com.medsentinel.auth;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;

public class AuthEventListener {

    private final AccessLogService accessLogService;

    public AuthEventListener(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    public void onLoginSuccess(String username, Long userId, String ip) {
        AccessLog entry = new AccessLog()
            .userId(userId)
            .usernameAttempted(username)
            .ip(ip != null ? ip : "127.0.0.1")
            .action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Authentication successful");

        accessLogService.logAccess(entry);
    }

    public void onLoginFailure(String username, String ip) {
        AccessLog entry = new AccessLog()
            .usernameAttempted(username)
            .ip(ip != null ? ip : "127.0.0.1")
            .action("LOGIN")
            .outcome(AccessLog.Outcome.FAILED)
            .reason("Bad credentials");

        accessLogService.logAccess(entry);
    }
}
