package com.edtech.platform.controller;

import com.edtech.platform.dto.ApiResponse;
import com.edtech.platform.dto.PaymentDto;
import com.edtech.platform.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── Plans (shown before user pays) ────────────────────────────────────────

    @GetMapping("/plans/student")
    public ResponseEntity<ApiResponse<List<PaymentDto.PricingPlan>>> studentPlans() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getStudentPlans()));
    }

    @GetMapping("/plans/teacher")
    public ResponseEntity<ApiResponse<List<PaymentDto.PricingPlan>>> teacherPlans() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getTeacherPlans()));
    }

    // ── Core payment flow ─────────────────────────────────────────────────────

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

    // ── Refund flow ────────────────────────────────────────────────────────────

    /** User requests a refund */
    @PostMapping("/refund/request")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> requestRefund(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PaymentDto.RefundRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Refund request submitted", paymentService.requestRefund(userId, req)));
    }

    /** Admin view — all pending refund requests */
    @GetMapping("/admin/refunds")
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> pendingRefunds() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPendingRefunds()));
    }

    /** Admin approves or rejects a refund */
    @PatchMapping("/admin/refunds/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentDto.Response>> processRefund(
            @PathVariable Long paymentId,
            @RequestParam boolean approve) {
        return ResponseEntity.ok(ApiResponse.ok(
                approve ? "Refund approved and processed" : "Refund rejected",
                paymentService.adminProcessRefund(paymentId, approve)));
    }

    /** User checks their payment + refund history */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentDto.Response>>> myPayments(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyPayments(userId)));
    }

    /** Check if student has active unlimited plan */
    @GetMapping("/has-unlimited-plan")
    public ResponseEntity<ApiResponse<Boolean>> hasUnlimitedPlan(
            @RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.hasActiveUnlimitedPlan(userId)));
    }
}