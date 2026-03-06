package com.ttwreis.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    private Double amount;

    /** Gateway transaction reference */
    @Column(length = 100)
    private String transactionId;

    /** Payment gateway name (e.g. Razorpay, SBI Collect) */
    @Column(length = 50)
    private String gateway;

    /** Raw response JSON from gateway */
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    /** Bank reference number */
    @Column(length = 100)
    private String bankRef;

    private LocalDateTime initiatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime failedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum PaymentStatus {
        PENDING, INITIATED, PAID, FAILED, REFUNDED
    }
}
