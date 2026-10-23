package com.medsentinel.auth;

import com.medsentinel.logging.AccessLog;
import com.medsentinel.logging.AccessLogService;
import com.medsentinel.rbac.Permission;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthService {

    private final UserRepository userRepo;
    private final AccessLogService accessLogService;

    public AuthService(UserRepository userRepo, AccessLogService accessLogService) {
        this.userRepo = userRepo;
        this.accessLogService = accessLogService;
    }

    public Optional<AppUserDetails> login(String username, String password, String ip) {
        if (ip == null || ip.isBlank()) ip = "127.0.0.1";

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return Optional.empty();
        }

        Optional<User> userOpt = userRepo.findByUsername(username);

        if (userOpt.isEmpty()) {
            accessLogService.logAccess(new AccessLog()
                .usernameAttempted(username)
                .ip(ip)
                .action("LOGIN")
                .outcome(AccessLog.Outcome.FAILED)
                .reason("User not found"));
            return Optional.empty();
        }

        User user = userOpt.get();
        if (user.getStatus() == User.UserStatus.DISABLED) {
            accessLogService.logAccess(new AccessLog()
                .userId(user.getUserId())
                .usernameAttempted(username)
                .ip(ip)
                .action("LOGIN")
                .outcome(AccessLog.Outcome.FAILED)
                .reason("User disabled"));
            return Optional.empty();
        }

        boolean valid = isValidPassword(password, user.getPwHash());

        if (!valid) {
            accessLogService.logAccess(new AccessLog()
                .userId(user.getUserId())
                .usernameAttempted(username)
                .ip(ip)
                .action("LOGIN")
                .outcome(AccessLog.Outcome.FAILED)
                .reason("Bad credentials"));
            return Optional.empty();
        }

        accessLogService.logAccess(new AccessLog()
            .userId(user.getUserId())
            .usernameAttempted(username)
            .ip(ip)
            .action("LOGIN")
            .outcome(AccessLog.Outcome.ALLOWED)
            .reason("Authentication successful"));

        return Optional.of(toUserDetails(user));
    }

    public AppUserDetails toUserDetails(User user) {
        if (user == null) {
            return null;
        }

        Set<String> perms = user.getRole() != null && user.getRole().getPermissions() != null
            ? user.getRole().getPermissions().stream()
                .filter(java.util.Objects::nonNull)
                .map(Permission::getCode)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet())
            : Set.of();

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";

        return new AppUserDetails(
            user.getUserId(),
            user.getUsername(),
            user.getPwHash(),
            roleName,
            user.getWardId(),
            perms
        );
    }

    private boolean isValidPassword(String password, String passwordHash) {
        if (password == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(password, passwordHash);
        } catch (IllegalArgumentException invalidHash) {
            return false;
        }
    }
}
