package com.ttwreis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Aadhaar number is required")
    @Pattern(regexp = "\\d{12}", message = "Aadhaar must be exactly 12 digits")
    private String aadhaarNumber;

    private String confirmAadhaar;

    @NotBlank(message = "Candidate name is required")
    private String candidateName;

    @NotBlank(message = "Father's name is required")
    private String fatherName;

    @NotBlank(message = "Mother's name is required")
    private String motherName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;

    private String confirmEmail;

    @NotBlank(message = "Gender is required")
    private String gender;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "[6-9]\\d{9}", message = "Invalid mobile number (10 digits, starts with 6-9)")
    private String mobileNumber;

    private String confirmMobile;
    private String alternateMobile;

    @NotBlank(message = "ID type is required")
    private String idType;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    // Captcha fields
    private String captchaId;
    private String captchaInput;

    // ── Present Address — @JsonProperty ensures Jackson binds "pCountry" → pCountry
    // regardless of how the JVM/Lombok generates getter names ─────────────────────
    @JsonProperty("pCountry")
    private String pCountry;

    @JsonProperty("pState")
    private String pState;

    @JsonProperty("pDistrict")
    private String pDistrict;

    @JsonProperty("pMandal")
    private String pMandal;

    @JsonProperty("pVillage")
    private String pVillage;

    @JsonProperty("pPincode")
    private String pPincode;

    // ── Permanent Address ─────────────────────────────────────────────────────
    @JsonProperty("permCountry")
    private String permCountry;

    @JsonProperty("permState")
    private String permState;

    @JsonProperty("permDistrict")
    private String permDistrict;

    @JsonProperty("permMandal")
    private String permMandal;

    @JsonProperty("permVillage")
    private String permVillage;

    @JsonProperty("permPincode")
    private String permPincode;

    // Extra frontend field — ignored silently
    private Boolean sameAsPresent;
}