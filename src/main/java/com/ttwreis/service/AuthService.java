package com.ttwreis.service;

import com.ttwreis.dto.AuthResponse;
import com.ttwreis.dto.LoginRequest;
import com.ttwreis.entity.User;
import com.ttwreis.repository.UserRepository;
import com.ttwreis.security.JwtUtil;
import com.ttwreis.security.Sha256Util;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil        jwtUtil;
    private final CaptchaService captchaService;

    public AuthResponse login(LoginRequest req) {

        // 1. Verify captcha first (single-use, 5-min TTL)
        if (!captchaService.verify(req.getCaptchaId(), req.getCaptchaInput())) {
            throw new IllegalArgumentException("Invalid or expired security code. Please refresh and try again.");
        }

        // 2. Look up user by registrationNumber or email
        User user = userRepository
                .findByRegistrationNumberOrEmail(req.getUsername(), req.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated. Please contact admin.");
        }

        // 3. Verify salted password payload
        // DB stores sha256(plainPassword).
        // Frontend sends sha256(salt + sha256(plainPassword)).
        // Sha256Util.verifyLoginPayload reconstructs the same hash server-side.
        boolean passwordOk = Sha256Util.verifyLoginPayload(
                user.getRegistrationNumber(),
                user.getPassword(),          // sha256(plainPassword) stored in DB
                req.getPassword());          // salted payload from frontend

        if (!passwordOk) {
            log.warn("[AUTH] Password mismatch for user={}", user.getRegistrationNumber());
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 4. Issue JWT
        String token = jwtUtil.generateToken(user.getRegistrationNumber(), user.getRole().name());
        log.info("[AUTH] Login success: user={} role={}", user.getRegistrationNumber(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .name(user.getCandidateName())
                .role(user.getRole().name())
                .registrationNumber(user.getRegistrationNumber())
                .email(user.getEmail())
                .userId(user.getId())
                .build();
    }
}
