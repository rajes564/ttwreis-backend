package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 30)
    private String applicationNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ── Academic ──────────────────────────────────────────────────────────────
    private String classApplied;
    private String stream;
    private String educationalStatus;

    // ── Caste & Community ─────────────────────────────────────────────────────
    private String community;
    private String subCaste;
    private Boolean incomeBelowThreshold;

    // ── College Priorities (separate table) ───────────────────────────────────
    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("priorityOrder ASC")
    @Builder.Default
    private List<CollegePriority> collegePriorities = new ArrayList<>();

    // ── Media ─────────────────────────────────────────────────────────────────
    @Column(columnDefinition = "TEXT")
    private String photoBase64;

    @Column(columnDefinition = "TEXT")
    private String signatureBase64;

    // ── Payment (separate table) ───────────────────────────────────────────────
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    // ── Status ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private ApplicationStatus status = ApplicationStatus.DRAFT;

    private LocalDateTime submittedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    // ── Exam / Allotment ──────────────────────────────────────────────────────
    private String examCenterName;
    private String examCenterAddress;
    private String hallTicketNumber;
    private Boolean appeared;
    private String allottedCollege;
    private String allottedStream;
    private String allotmentPhase;
    private String joiningDate;

    // ── Convenience: payment status from linked Payment table ─────────────────
    public Payment.PaymentStatus getPaymentStatus() {
        return payment != null ? payment.getStatus() : Payment.PaymentStatus.PENDING;
    }

    public Double getAmountPaid() {
        return payment != null && payment.getStatus() == Payment.PaymentStatus.PAID
               ? payment.getAmount() : null;
    }

    public enum ApplicationStatus {
        DRAFT, SUBMITTED, PAYMENT_PENDING, PAID, VERIFIED, ALLOTTED
    }
}
