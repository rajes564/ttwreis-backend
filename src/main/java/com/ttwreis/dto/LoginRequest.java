package com.ttwreis.dto;

import lombok.Data;

@Data
public class LoginRequest {
    /** registrationNumber or email */
    private String username;

    /**
     * Salted hash from the frontend (NOT plain text):
     *   sha256( salt + sha256(plainPassword) )
     *   where salt = sha256(username + ":" + APP_SECRET + ":" + timeSlot)
     */
    private String password;

    /** UUID received from GET /api/captcha/generate */
    private String captchaId;

    /** The text the user typed matching the captcha image */
    private String captchaInput;
}
