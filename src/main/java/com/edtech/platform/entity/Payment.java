package com.edtech.platform.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    private Long referenceId;

    // ── Extra fields for manual/cash transactions and ledger ──────────────────
    private String userName;        // For manual cash entries (person not in system)
    private String userType;        // STUDENT / TEACHER / SCHOOL / OTHER
    private String paymentMode;     // ONLINE / CASH / BANK_TRANSFER / UPI
    private String purpose;         // TEST_PURCHASE / WORKSHOP / TEACHER_FEE / MANUAL_CASH / etc.
    private String note;            // Admin remarks
    private String referenceNumber; // Cash receipt or manual reference

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = PaymentStatus.CREATED;
    }

    public enum PaymentStatus { CREATED, SUCCESS, FAILED, PENDING, REFUNDED }
    public enum PaymentType   { STUDENT_PLATFORM_FEE, TEACHER_SUBSCRIPTION, SESSION_BOOKING, WORKSHOP, TEST_PURCHASE, SCHOOL_PACKAGE, WEBINAR, COURSE, MANUAL_CASH, OTHER }
}