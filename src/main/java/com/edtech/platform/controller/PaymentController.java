package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.PaymentDto;
import com.edtech.platform.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentDto.OrderResponse>> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PaymentDto.CreateOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.createOrder(userId, req)));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> verify(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PaymentDto.VerifyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Payment verified", paymentService.verifyPayment(userId, req)));
    }
}
