package com.ttwreis.controller;

import com.ttwreis.dto.RegistrationRequest;
import com.ttwreis.dto.RegistrationResponse;
import com.ttwreis.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    /**
     * POST /api/registration/register  (public – no JWT needed)
     */
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(
            @Valid @RequestBody RegistrationRequest request) {
    	
    	System.out.println(request);
        return ResponseEntity.ok(userService.register(request));
    }
}
