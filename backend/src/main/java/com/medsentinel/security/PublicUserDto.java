package com.medsentinel.security;

import com.medsentinel.auth.AppUserDetails;
import com.medsentinel.auth.User;
import com.medsentinel.rbac.Permission;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/** Allowlisted user representation. It intentionally has no password field. */
public record PublicUserDto(
    Long userId,
    String username,
    String role,
    Long wardId,
    String status,
    Set<String> permissions
) {

    public static PublicUserDto from(AppUserDetails user) {
        if (user == null) {
            return null;
        }
        return new PublicUserDto(
            user.getUserId(),
            user.getUsername(),
            user.getRoleName(),
            user.getWardId(),
            "ACTIVE",
            immutableCodes(user.getPermissions())
        );
    }

    public static PublicUserDto from(User user) {
        if (user == null) {
            return null;
        }
        Set<String> permissions = new LinkedHashSet<>();
        if (user.getRole() != null && user.getRole().getPermissions() != null) {
            for (Permission permission : user.getRole().getPermissions()) {
                if (permission != null && permission.getCode() != null) {
                    permissions.add(permission.getCode());
                }
            }
        }
        return new PublicUserDto(
            user.getUserId(),
            user.getUsername(),
            user.getRole() != null ? user.getRole().getName() : "USER",
            user.getWardId(),
            user.getStatus() != null ? user.getStatus().name() : null,
            Set.copyOf(permissions)
        );
    }

    private static Set<String> immutableCodes(Collection<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return Set.of();
        }
        return Set.copyOf(codes);
    }
}
