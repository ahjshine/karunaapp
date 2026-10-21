package com.medsentinel.auth;

import com.medsentinel.logging.AccessLogService;
import com.medsentinel.logging.AccessLog;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Listens to Spring Security authentication events and logs them.
 */
@Component
public class AuthEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);
    private final AccessLogService accessLogService;

    public AuthEventListener(AccessLogService accessLogService) {
        this.accessLogService = accessLogService;
    }

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIp();
        log.info("Login success: user={}, ip={}", username, ip);

        AccessLog entry = new AccessLog()
            .usernameAttempted(username)
            .ip(ip)
            .action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Authentication successful");

        // Set userId from AppUserDetails if available
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof AppUserDetails details) {
            entry.userId(details.getUserId());
        }

        accessLogService.logAccess(entry);
    }

    @EventListener
    public void onFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String ip = getClientIp();
        log.warn("Login failure: user={}, ip={}", username, ip);

        AccessLog entry = new AccessLog()
            .usernameAttempted(username)
            .ip(ip)
            .action("LOGIN")
            .outcome(AccessLog.Outcome.FAILED)
            .reason("Bad credentials");

        accessLogService.logAccess(entry);
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xff = request.getHeader("X-Forwarded-For");
                return xff != null ? xff.split(",")[0].trim() : request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not resolve client IP", e);
        }
        return "127.0.0.1";
    }
}
