package com.ttwreis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side CAPTCHA service.
 *
 * Flow:
 *   1. Client calls GET /api/captcha/generate → receives { captchaId, imageText }
 *      (imageText is the visible text; in a real app you'd render it as an image)
 *   2. Client submits captchaId + userInput with the login request.
 *   3. Server calls verify(captchaId, userInput) before authenticating.
 *
 * Captchas expire after EXPIRY_MS (5 minutes) and are single-use.
 */
@Service
@EnableScheduling
@Slf4j
public class CaptchaService {

    private static final int    CAPTCHA_LENGTH = 6;
    private static final long   EXPIRY_MS      = 5 * 60 * 1000L; // 5 minutes
    private static final String CHARS          = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no I/O/0/1

    private final SecureRandom rng = new SecureRandom();

    // captchaId → { text, createdAt }
    private final Map<String, CaptchaEntry> store = new ConcurrentHashMap<>();

    // ── Generate ──────────────────────────────────────────────────────────────

    public CaptchaResult generate() {
        String id   = UUID.randomUUID().toString();
        String text = randomText();
        store.put(id, new CaptchaEntry(text, System.currentTimeMillis()));
        log.debug("[CAPTCHA] generated id={} text={}", id, text);
        return new CaptchaResult(id, text);
    }

    // ── Verify ────────────────────────────────────────────────────────────────

    /**
     * Returns true if the captchaId exists, is not expired, and the input matches.
     * Removes the entry after verification (single-use).
     */
    public boolean verify(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) return false;

        CaptchaEntry entry = store.remove(captchaId); // single-use
        if (entry == null) {
            log.warn("[CAPTCHA] id not found: {}", captchaId);
            return false;
        }
        if (System.currentTimeMillis() - entry.createdAt() > EXPIRY_MS) {
            log.warn("[CAPTCHA] expired: {}", captchaId);
            return false;
        }
        boolean ok = entry.text().equalsIgnoreCase(userInput.trim());
        if (!ok) log.warn("[CAPTCHA] mismatch id={} expected={} got={}", captchaId, entry.text(), userInput);
        return ok;
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    /** Purge expired entries every 10 minutes */
    @Scheduled(fixedDelay = 600_000)
    public void evictExpired() {
        long now   = System.currentTimeMillis();
        int  before = store.size();
        store.entrySet().removeIf(e -> now - e.getValue().createdAt() > EXPIRY_MS);
        int removed = before - store.size();
        if (removed > 0) log.debug("[CAPTCHA] evicted {} expired entries", removed);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String randomText() {
        StringBuilder sb = new StringBuilder(CAPTCHA_LENGTH);
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            sb.append(CHARS.charAt(rng.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    // ── Records ───────────────────────────────────────────────────────────────

    public record CaptchaResult(String captchaId, String text) {}
    private record CaptchaEntry(String text, long createdAt) {}
}
