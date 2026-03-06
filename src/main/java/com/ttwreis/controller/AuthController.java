package com.ttwreis.controller;

import com.ttwreis.dto.AuthResponse;
import com.ttwreis.dto.LoginRequest;
import com.ttwreis.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/auth/login
     * Body: {
     *   username:     "TTWREIS2025000001",
     *   password:     "<salted-sha256-payload>",
     *   captchaId:    "uuid-from-generate",
     *   captchaInput: "AB3X7K"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
