package com.eaglebank.service.security;

import com.eaglebank.domain.UserEntity;
import com.eaglebank.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Bridges your user store to Spring Security.
 * Username == email. Password must be a hash compatible with your PasswordEncoder.
 */
@Service
@RequiredArgsConstructor
public class StoreUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find by email; adapt type to whatever your service returns (BO or Entity).
        UserEntity u = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return User.withUsername(u.getEmail())
                .password(u.getPasswordHash())
                .roles("USER")
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
