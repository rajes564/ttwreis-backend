package com.ttwreis.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String name;
    private String role;
    private String registrationNumber;
    private String email;
    private Long   userId;
}
