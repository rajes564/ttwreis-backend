package com.ttwreis.security;

import com.ttwreis.entity.User;
import com.ttwreis.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Used only by JwtAuthFilter to load user details after token validation.
 * Login authentication is handled manually in AuthService (SHA-256 + salt).
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String registrationNumber) throws UsernameNotFoundException {
        User user = userRepository
                .findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + registrationNumber));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("Account deactivated: " + registrationNumber);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getRegistrationNumber(),
                user.getPassword(),   // sha256 hash – only used as a non-null placeholder
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
