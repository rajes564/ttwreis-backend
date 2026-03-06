package com.ttwreis.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Stateless SHA-256 helper.
 *
 * Password storage scheme (mirrors the frontend):
 *
 *   stored_password = sha256(plainPassword)
 *
 * Login verification scheme (time-windowed salted hash):
 *
 *   Frontend:
 *     timeSlot  = floor(epochSeconds / TIME_WINDOW)        // 3600 s = 1 h
 *     salt      = sha256(username + ":" + APP_SECRET + ":" + timeSlot)
 *     innerHash = sha256(plainPassword)
 *     payload   = sha256(salt + innerHash)
 *
 *   Backend reconstructs the same value and compares:
 *     stored    = sha256(plainPassword)  (from DB)
 *     salt      = sha256(username + ":" + APP_SECRET + ":" + timeSlot)
 *     expected  = sha256(salt + stored)
 *     ok        = (payload == expected)
 *
 * The salt changes every TIME_WINDOW seconds, so replayed tokens expire
 * automatically.  We check both the current window and the previous one
 * to avoid edge-case failures near a window boundary.
 */
public final class Sha256Util {

    public static final String APP_SECRET   = "iGrShYdErAbAdRaJeShNiC";
    public static final long   TIME_WINDOW  = 3600L; // seconds

    private Sha256Util() {}

    // ── Core ─────────────────────────────────────────────────────────────────

    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── Password storage ──────────────────────────────────────────────────────

    /** Hash a plain-text password for DB storage. */
    public static String hashPassword(String plainPassword) {
        return sha256(plainPassword);
    }

    // ── Login verification ────────────────────────────────────────────────────

    /**
     * Returns true if the frontend payload matches the expected salted hash
     * for the given username and stored password hash.
     *
     * Checks both current and previous time-window to handle boundary clock skew.
     *
     * @param username       registrationNumber sent by the client
     * @param storedHash     sha256(plainPassword) stored in DB
     * @param clientPayload  sha256(salt + sha256(plainPassword)) from frontend
     */
    public static boolean verifyLoginPayload(String username,
                                             String storedHash,
                                             String clientPayload) {
        long currentSlot  = System.currentTimeMillis() / 1000L / TIME_WINDOW;
        long previousSlot = currentSlot - 1;

        return matchesSlot(username, storedHash, clientPayload, currentSlot)
            || matchesSlot(username, storedHash, clientPayload, previousSlot);
    }

    private static boolean matchesSlot(String username,
                                       String storedHash,
                                       String clientPayload,
                                       long   timeSlot) {
        // Mirrors frontend: sha256(`${username}:${APP_SECRET}:${timeSlot}`)
        String salt     = sha256(username + ":" + APP_SECRET + ":" + timeSlot);
        // Mirrors frontend: sha256(salt + innerHash)
        // innerHash on the frontend = sha256(plainPassword) = storedHash
        String expected = sha256(salt + storedHash);
        return expected.equals(clientPayload);
    }
}
