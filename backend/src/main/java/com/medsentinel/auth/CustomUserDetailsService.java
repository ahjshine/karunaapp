package com.medsentinel.auth;

import com.medsentinel.rbac.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getStatus() == User.UserStatus.DISABLED) {
            throw new UsernameNotFoundException("User disabled: " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        // Add role as ROLE_<name> for Spring Security hasRole() checks
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

        // Add individual permissions
        for (Permission perm : user.getRole().getPermissions()) {
            authorities.add(new SimpleGrantedAuthority(perm.getCode()));
        }

        return new AppUserDetails(
            user.getUserId(),
            user.getUsername(),
            user.getPwHash(),
            user.getRole().getName(),
            user.getWardId(),
            authorities
        );
    }
}
