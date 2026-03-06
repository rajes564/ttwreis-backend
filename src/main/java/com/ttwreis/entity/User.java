package com.ttwreis.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String registrationNumber;

    @Column(unique = true, nullable = false, length = 12)
    private String aadhaarNumber;

    @Column(nullable = false, length = 100)
    private String candidateName;

    @Column(length = 100)
    private String fatherName;

    @Column(length = 100)
    private String motherName;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 15)
    private String mobileNumber;

    @Column(length = 15)
    private String alternateMobile;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Gender gender;

    @Column(length = 60)
    private String idType;

    @Column(length = 60)
    private String idNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    // ── Present Address — explicit column names prevent any Hibernate naming ambiguity ──
    @Column(name = "p_country",  length = 60)  private String pCountry;
    @Column(name = "p_state",    length = 60)  private String pState;
    @Column(name = "p_district", length = 60)  private String pDistrict;
    @Column(name = "p_mandal",   length = 60)  private String pMandal;
    @Column(name = "p_village",  length = 100) private String pVillage;
    @Column(name = "p_pincode",  length = 10)  private String pPincode;

    // ── Permanent Address ──────────────────────────────────────────────────────
    @Column(name = "perm_country",  length = 60)  private String permCountry;
    @Column(name = "perm_state",    length = 60)  private String permState;
    @Column(name = "perm_district", length = 60)  private String permDistrict;
    @Column(name = "perm_mandal",   length = 60)  private String permMandal;
    @Column(name = "perm_village",  length = 100) private String permVillage;
    @Column(name = "perm_pincode",  length = 10)  private String permPincode;

    // ── Bidirectional link to Application (one candidate → one application) ────
    // mappedBy="user" means Application.user is the FK owner; we just navigate back
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, optional = true)
    @JsonIgnore   // prevent infinite serialisation loops if entity is ever JSON-serialised directly
    private Application application;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum Gender { Male, Female, Other }
    public enum Role   { CANDIDATE, ADMIN, PRINCIPAL, EXAM_CELL, DATA_ENTRY }
}