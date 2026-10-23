package com.medsentinel.auth;

import com.medsentinel.rbac.Permission;

import java.util.HashSet;
import java.util.Set;

public class CustomUserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AppUserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new RuntimeException("User disabled: " + username);
        }

        Set<String> authorities = new HashSet<>();
        if (user.getRole() != null) {
            authorities.add("ROLE_" + user.getRole().getName());
            if (user.getRole().getPermissions() != null) {
                for (Permission perm : user.getRole().getPermissions()) {
                    authorities.add(perm.getCode());
                }
            }
        }

        return new AppUserDetails(
            user.getUserId(),
            user.getUsername(),
            user.getPwHash(),
            user.getRole() != null ? user.getRole().getName() : "USER",
            user.getWardId(),
            authorities
        );
    }
}
