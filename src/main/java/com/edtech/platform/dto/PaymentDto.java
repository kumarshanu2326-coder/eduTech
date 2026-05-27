package com.edtech.platform.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class CreateOrderRequest {
        private BigDecimal amount;
        private String     type;        // STUDENT_PLATFORM_FEE | TEACHER_SUBSCRIPTION | SESSION_BOOKING | WORKSHOP
        private Long       referenceId;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OrderResponse {
        private String     orderId;
        private BigDecimal amount;
        private String     currency;
        private String     keyId;       // Razorpay public key for frontend
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class VerifyRequest {
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpaySignature;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class Response {
        private Long       id;
        private String     razorpayOrderId;
        private String     razorpayPaymentId;
        private BigDecimal amount;
        private String     status;
        private String     type;
        private LocalDateTime createdAt;
    }
}
