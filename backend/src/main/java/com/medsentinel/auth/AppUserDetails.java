package com.medsentinel.auth;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User details and permissions context for authenticated sessions.
 */
public class AppUserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String roleName;
    private final Long wardId;
    private final Set<String> permissions;
    private final Set<String> roles;

    public AppUserDetails(Long userId, String username, String password,
                          String roleName, Long wardId,
                          Collection<?> permissionsOrAuthorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.wardId = wardId;
        Set<String> permissionCodes = new java.util.HashSet<>();
        Set<String> roleAuthorities = new HashSet<>();
        if (permissionsOrAuthorities != null) {
            for (Object obj : permissionsOrAuthorities) {
                if (obj != null) {
                    String authority = obj.toString();
                    if (authority.startsWith("ROLE_")) {
                        roleAuthorities.add(authority.substring("ROLE_".length()));
                    } else {
                        permissionCodes.add(authority);
                    }
                }
            }
        }
        this.permissions = Set.copyOf(permissionCodes);
        this.roles = Set.copyOf(roleAuthorities);
    }

    public Long getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRoleName() { return roleName; }
    public Long getWardId() { return wardId; }
    public Set<String> getPermissions() { return permissions; }
    public Set<String> getRoles() { return roles; }

    public boolean hasPermission(String permissionCode) {
        return permissionCode != null && permissions.contains(permissionCode);
    }

    public boolean hasRole(String role) {
        if (role == null) {
            return false;
        }
        return role.equalsIgnoreCase(roleName)
            || roles.stream().anyMatch(candidate -> role.equalsIgnoreCase(candidate));
    }
}
