package com.medsentinel.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Custom UserDetails that carries userId, role name, and ward info
 * for use throughout the application.
 */
public class AppUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String roleName;
    private final Long wardId;
    private final List<GrantedAuthority> authorities;

    public AppUserDetails(Long userId, String username, String password,
                          String roleName, Long wardId,
                          List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.roleName = roleName;
        this.wardId = wardId;
        this.authorities = authorities;
    }

    public Long getUserId() { return userId; }
    public String getRoleName() { return roleName; }
    public Long getWardId() { return wardId; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String getUsername() { return username; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public boolean hasPermission(String permissionCode) {
        return authorities.stream()
            .anyMatch(a -> a.getAuthority().equals(permissionCode));
    }
}
