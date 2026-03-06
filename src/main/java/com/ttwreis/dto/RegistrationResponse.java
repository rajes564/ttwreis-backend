package com.ttwreis.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationResponse {
    private String registrationNumber;
    private String mobile;
    private String message;
}
