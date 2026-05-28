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
    private String razorpayRefundId;       // set when refund is processed

    @Column(nullable = false)
    private BigDecimal amount;

    private BigDecimal refundAmount;       // partial or full refund amount
    private BigDecimal cancellationFee;    // deducted from refund

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentType type;

    private Long   referenceId;            // leadId / teacherId / workshopId

    // Manual/offline fields
    private String userName;
    private String userType;
    private String paymentMode;            // ONLINE / CASH / UPI / BANK_TRANSFER
    private String purpose;
    private String note;
    private String referenceNumber;        // cash receipt or manual ref

    // Refund tracking
    private String refundStatus;           // NONE / REQUESTED / PROCESSED / REJECTED
    private String refundReason;
    private LocalDateTime refundRequestedAt;
    private LocalDateTime refundProcessedAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (status       == null) status       = PaymentStatus.CREATED;
        if (refundStatus == null) refundStatus  = "NONE";
        if (paymentMode  == null) paymentMode   = "ONLINE";
    }

    public enum PaymentStatus {
        CREATED, SUCCESS, FAILED, REFUNDED, PARTIALLY_REFUNDED, PENDING
    }

    public enum PaymentType {
        // Student
        DEMO_CLASS_SINGLE,          // ₹10 per demo (non-refundable unless poor experience)
        STUDENT_UNLIMITED_PLAN,     // ₹49 — all demos free for 6 months
        // Teacher
        TEACHER_SUBSCRIPTION,       // ₹499/month — basic listing + leads
        TEACHER_ELITE,              // ₹999 — elite list for webinars/workshops
        // Platform
        WORKSHOP_ENROLLMENT,
        SCHOOL_PACKAGE,
        WEBINAR_FEE,
        COURSE_PURCHASE,
        // Manual
        MANUAL_CASH,
        OTHER,
        // Legacy
        STUDENT_PLATFORM_FEE,
        SESSION_BOOKING,
        WORKSHOP
    }
}