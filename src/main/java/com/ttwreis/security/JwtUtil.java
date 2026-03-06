package com.ttwreis.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT helper using JJWT 0.11.5 API.
 *
 * Key points:
 *  - Jwts.builder()  then  .setSubject() / .claim() / .signWith()  ← 0.11.x style
 *  - Jwts.parserBuilder().setSigningKey(key).build()                ← 0.11.x style
 *    (NOT .verifyWith() which is 0.12.x only)
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    // ── Key ──────────────────────────────────────────────────────────────────

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // ── Generate ─────────────────────────────────────────────────────────────

    /**
     * @param registrationNumber  the login username stored as JWT subject
     * @param role                e.g. "CANDIDATE" or "ADMIN"
     */
    public String generateToken(String registrationNumber, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(registrationNumber)        // ← setSubject, not subject()
                .claim("role", role)                   // ← single .claim(), not claims(Map)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ── Parse ─────────────────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()               // ← parserBuilder(), NOT parser()
                .setSigningKey(signingKey())       // ← setSigningKey(), NOT verifyWith()
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /** Returns the registrationNumber stored as the JWT subject. */
    public String extractRegistrationNumber(String token) {
        return parseClaims(token).getSubject();
    }

    /** Returns the role claim stored in the token. */
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /** Returns true if the token signature is valid and not expired. */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);   // throws on invalid/expired
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
