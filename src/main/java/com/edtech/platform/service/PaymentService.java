package com.edtech.platform.service;

import com.edtech.platform.dto.PaymentDto;
import com.edtech.platform.entity.Payment;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.PaymentRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment service using Razorpay test credentials.
 *
 * Production switch:
 * 1. Replace razorpay.key.id and razorpay.key.secret in application.properties
 * 2. Uncomment the Razorpay SDK calls below (add dependency to pom.xml when ready)
 * 3. Implement real HMAC-SHA256 signature verification in verifyPayment()
 */
@Service
public class PaymentService {

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:secret_placeholder}")
    private String razorpayKeySecret;

    private final PaymentRepository paymentRepository;
    private final UserRepository    userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository    = userRepository;
    }

    @Transactional
    public PaymentDto.OrderResponse createOrder(Long userId, PaymentDto.CreateOrderRequest req) {
        User user = getUser(userId);

        // ── PRODUCTION: Uncomment when Razorpay SDK is added ──────────────────
        // RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        // JSONObject orderReq = new JSONObject();
        // orderReq.put("amount", req.getAmount().multiply(BigDecimal.valueOf(100)).intValue()); // paise
        // orderReq.put("currency", "INR");
        // orderReq.put("receipt", "rcpt_" + UUID.randomUUID().toString().substring(0,8));
        // Order order = client.orders.create(orderReq);
        // String orderId = order.get("id");
        // ──────────────────────────────────────────────────────────────────────

        // Test mode — generate a dummy order ID
        String orderId = "order_test_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        Payment.PaymentType type;
        try { type = Payment.PaymentType.valueOf(req.getType().toUpperCase()); }
        catch (Exception e) { type = Payment.PaymentType.STUDENT_PLATFORM_FEE; }

        Payment payment = Payment.builder()
                .user(user)
                .razorpayOrderId(orderId)
                .amount(req.getAmount())
                .type(type)
                .referenceId(req.getReferenceId())
                .status(Payment.PaymentStatus.CREATED)
                .build();
        paymentRepository.save(payment);

        return PaymentDto.OrderResponse.builder()
                .orderId(orderId)
                .amount(req.getAmount())
                .currency("INR")
                .keyId(razorpayKeyId)
                .build();
    }

    @Transactional
    public PaymentDto.Response verifyPayment(Long userId, PaymentDto.VerifyRequest req) {
        Payment payment = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // ── PRODUCTION: Uncomment real signature verification ─────────────────
        // String generated = Utils.getHash(req.getRazorpayOrderId() + "|" + req.getRazorpayPaymentId(),
        //                                   razorpayKeySecret);
        // if (!generated.equals(req.getRazorpaySignature()))
        //     throw new IllegalArgumentException("Payment verification failed");
        // ──────────────────────────────────────────────────────────────────────

        // Test mode — auto-verify
        payment.setRazorpayPaymentId(req.getRazorpayPaymentId());
        payment.setRazorpaySignature(req.getRazorpaySignature());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment = paymentRepository.save(payment);

        return toResponse(payment);
    }

    private PaymentDto.Response toResponse(Payment p) {
        return PaymentDto.Response.builder()
                .id(p.getId())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .amount(p.getAmount())
                .status(p.getStatus() != null ? p.getStatus().name() : "CREATED")
                .type(p.getType() != null ? p.getType().name() : "")
                .createdAt(p.getCreatedAt())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
