package com.ttwreis.service;

import com.ttwreis.dto.RegistrationRequest;
import com.ttwreis.dto.RegistrationResponse;
import com.ttwreis.entity.User;
import com.ttwreis.repository.UserRepository;
import com.ttwreis.security.Sha256Util;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final DateTimeFormatter DOB_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Transactional
    public RegistrationResponse register(RegistrationRequest req) {
    	
    	System.out.println(req.toString());

        if (!req.getAadhaarNumber().equals(req.getConfirmAadhaar()))
            throw new IllegalArgumentException("Aadhaar numbers do not match");
        if (!req.getEmail().equalsIgnoreCase(req.getConfirmEmail()))
            throw new IllegalArgumentException("Email addresses do not match");
        if (!req.getMobileNumber().equals(req.getConfirmMobile()))
            throw new IllegalArgumentException("Mobile numbers do not match");

        if (userRepository.existsByAadhaarNumber(req.getAadhaarNumber()))
            throw new IllegalArgumentException("Aadhaar number is already registered");
        if (userRepository.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email address is already registered");
        if (userRepository.existsByMobileNumber(req.getMobileNumber()))
            throw new IllegalArgumentException("Mobile number is already registered");

        String regNo      = generateUniqueRegistrationNumber();
        String defaultPw  = req.getDateOfBirth().format(DOB_FMT);
        String hashedPw   = Sha256Util.hashPassword(defaultPw);

        User user = User.builder()
                .registrationNumber(regNo)
                .aadhaarNumber (req.getAadhaarNumber())
                .candidateName (req.getCandidateName())
                .fatherName    (req.getFatherName())
                .motherName    (req.getMotherName())
                .dateOfBirth   (req.getDateOfBirth())
                .email         (req.getEmail())
                .mobileNumber  (req.getMobileNumber())
                .alternateMobile(req.getAlternateMobile())
                .gender        (parseGender(req.getGender()))
                .idType        (req.getIdType())
                .idNumber      (req.getIdNumber())
                .password      (hashedPw)
                .role          (User.Role.CANDIDATE)
                .active        (true)
                // ── Address fields from registration ──────────────────────
                .pCountry  (req.getPCountry())
                .pState    (req.getPState())
                .pDistrict (req.getPDistrict())
                .pMandal   (req.getPMandal())
                .pVillage  (req.getPVillage())
                .pPincode  (req.getPPincode())

                
                .permCountry  (req.getPermCountry())
                .permState    (req.getPermState())
                .permDistrict (req.getPermDistrict())
                .permMandal   (req.getPermMandal())
                .permVillage  (req.getPermVillage())
                .permPincode  (req.getPermPincode())
                .build();

        userRepository.save(user);

        return RegistrationResponse.builder()
                .registrationNumber(regNo)
                .mobile(req.getMobileNumber())
                .message("Registration successful! Login with Reg No: " + regNo + " and your Date of Birth as password.")
                .build();
    }

    private String generateUniqueRegistrationNumber() {
        int year = LocalDate.now().getYear();
        Random rng = new Random();
        String candidate;
        do {
            candidate = "TTWREIS" + year + String.format("%06d", rng.nextInt(999_999) + 1);
        } while (userRepository.existsByRegistrationNumber(candidate));
        return candidate;
    }

    private User.Gender parseGender(String g) {
        if (g == null) return null;
        try { return User.Gender.valueOf(g); }
        catch (IllegalArgumentException e) { return User.Gender.Other; }
    }
}
