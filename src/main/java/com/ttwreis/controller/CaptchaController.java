package com.ttwreis.controller;

import com.ttwreis.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * GET /api/captcha/generate  (public)
     * Returns: { captchaId: "uuid", text: "AB3X7K" }
     *
     * The frontend renders the text as a styled visual.
     * On login the frontend sends captchaId + captchaInput alongside credentials.
     */
    @GetMapping("/generate")
    public ResponseEntity<Map<String, String>> generate() {
        CaptchaService.CaptchaResult result = captchaService.generate();
        return ResponseEntity.ok(Map.of(
            "captchaId", result.captchaId(),
            "text",      result.text()
        ));
    }
}
