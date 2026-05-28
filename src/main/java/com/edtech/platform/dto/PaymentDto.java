package com.edtech.platform.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        private BigDecimal amount;
        private String     type;         // PaymentType enum name
        private Long       referenceId;  // leadId / teacherId / workshopId
        private String     note;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private String     orderId;
        private BigDecimal amount;
        private String     currency;
        private String     keyId;
        private String     type;
        private String     planDescription;   // shown to user in UI
        private String     refundPolicy;      // shown to user before paying
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VerifyRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long          id;
        private String        razorpayOrderId;
        private String        razorpayPaymentId;
        private String        razorpayRefundId;
        private BigDecimal    amount;
        private BigDecimal    refundAmount;
        private String        status;
        private String        type;
        private String        refundStatus;
        private String        refundReason;
        private LocalDateTime createdAt;
        private LocalDateTime refundRequestedAt;
        private LocalDateTime refundProcessedAt;
    }

    /** Student/teacher requests a refund */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RefundRequest {
        private Long   paymentId;
        private String reason;       // "DEMO_UNSATISFIED" / "CANCELLED" / "NO_LEADS" / "OTHER"
        private String details;      // free text
    }

    /** Pricing plans returned to frontend */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PricingPlan {
        private String     id;
        private String     title;
        private BigDecimal price;
        private String     description;
        private String     refundPolicy;
        private String     paymentType;
        private boolean    recommended;
        private java.util.List<String> features;
    }
}