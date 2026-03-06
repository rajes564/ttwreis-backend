package com.ttwreis.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "grievances")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Grievance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String grievanceNumber;

    // Filer details (anyone can file — no login required)
    @Column(nullable = false, length = 100)
    private String applicantName;

    @Column(nullable = false, length = 15)
    private String mobileNumber;

    @Column(length = 100)
    private String email;

    @Column(length = 30)
    private String registrationNumber; // optional — if they have one

    @Column(nullable = false, length = 60)
    private String category; // e.g. Registration Issue, Payment Issue, etc.

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GrievanceStatus status = GrievanceStatus.OPEN;

    // Admin action
    @Column(columnDefinition = "TEXT")
    private String adminRemarks;

    @Column(length = 100)
    private String resolvedBy;

    private LocalDateTime resolvedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum GrievanceStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }
}
