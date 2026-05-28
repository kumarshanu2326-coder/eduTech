package com.edtech.platform.service;

import com.edtech.platform.dto.PaymentDto;
import com.edtech.platform.entity.Payment;
import com.edtech.platform.entity.User;
import com.edtech.platform.repository.PaymentRepository;
import com.edtech.platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Value("${razorpay.key.id:rzp_test_placeholder}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:secret_placeholder}")
    private String razorpayKeySecret;

    private final PaymentRepository paymentRepository;
    private final UserRepository    userRepository;
    private final RestTemplate      restTemplate = new RestTemplate();

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository    = userRepository;
    }

    // ── Pricing plans ──────────────────────────────────────────────────────────

    public List<PaymentDto.PricingPlan> getStudentPlans() {
        return List.of(
                PaymentDto.PricingPlan.builder()
                        .id("DEMO_SINGLE").title("Single Demo")
                        .price(new BigDecimal("10"))
                        .paymentType("DEMO_CLASS_SINGLE")
                        .description("Book 1 demo class with any teacher")
                        .refundPolicy("Refundable if you are unsatisfied with the demo experience. Non-refundable for no-shows or cancellations.")
                        .recommended(false)
                        .features(List.of("1 demo class booking", "Teacher notified instantly", "Refundable if demo was unsatisfying", "Prevents fake/spam bookings"))
                        .build(),
                PaymentDto.PricingPlan.builder()
                        .id("UNLIMITED_PLAN").title("Unlimited Plan")
                        .price(new BigDecimal("49"))
                        .paymentType("STUDENT_UNLIMITED_PLAN")
                        .description("All demo classes free for 6 months")
                        .refundPolicy("Full refund within 7 days. 30% cancellation fee if cancelled within 30 days. No refund after 30 days.")
                        .recommended(true)
                        .features(List.of("Unlimited demo bookings", "Valid for 6 months", "Save ₹41+ vs individual demos", "Priority teacher matching"))
                        .build()
        );
    }

    public List<PaymentDto.PricingPlan> getTeacherPlans() {
        return List.of(
                PaymentDto.PricingPlan.builder()
                        .id("TEACHER_BASIC").title("Basic Listing")
                        .price(new BigDecimal("499"))
                        .paymentType("TEACHER_SUBSCRIPTION")
                        .description("Get verified and receive student leads every month")
                        .refundPolicy("50% refund if you receive zero leads within 30 days. We do not guarantee leads — they depend on geography, subject demand, and profile quality.")
                        .recommended(true)
                        .features(List.of("Verified badge on profile", "Appear in student search", "Receive demo requests", "Profile analytics dashboard", "50% refund if 0 leads in 30 days"))
                        .build(),
                PaymentDto.PricingPlan.builder()
                        .id("TEACHER_ELITE").title("Elite Teacher")
                        .price(new BigDecimal("999"))
                        .paymentType("TEACHER_ELITE")
                        .description("Join the elite pool for workshops and school programmes")
                        .refundPolicy("Non-refundable after activation. Elite status subject to annual renewal and subject expertise verification.")
                        .recommended(false)
                        .features(List.of(
                                "Everything in Basic plan",
                                "Eligible for school workshops",
                                "Invited to platform webinars",
                                "Bulk opportunity notifications",
                                "EduConnect Certified Expert badge",
                                "Priority lead routing"
                        ))
                        .build()
        );
    }

    // ── Create Razorpay order ──────────────────────────────────────────────────

    @Transactional
    public PaymentDto.OrderResponse createOrder(Long userId, PaymentDto.CreateOrderRequest req) {
        User user = getUser(userId);
        String orderId = "order_test_" + UUID.randomUUID().toString().replace("-","").substring(0,16);

        Payment.PaymentType type;
        try   { type = Payment.PaymentType.valueOf(req.getType().toUpperCase()); }
        catch (Exception e) { type = Payment.PaymentType.OTHER; }

        paymentRepository.save(Payment.builder()
                .user(user).razorpayOrderId(orderId)
                .amount(req.getAmount()).type(type)
                .referenceId(req.getReferenceId()).note(req.getNote())
                .status(Payment.PaymentStatus.CREATED).build());

        return PaymentDto.OrderResponse.builder()
                .orderId(orderId).amount(req.getAmount())
                .currency("INR").keyId(razorpayKeyId).type(req.getType())
                .planDescription(getPlanDescription(type))
                .refundPolicy(getRefundPolicyText(type))
                .build();
    }

    // ── Verify payment ─────────────────────────────────────────────────────────

    @Transactional
    public PaymentDto.Response verifyPayment(Long userId, PaymentDto.VerifyRequest req) {
        Payment p = paymentRepository.findByRazorpayOrderId(req.getRazorpayOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // PRODUCTION: verify HMAC-SHA256 signature here using razorpayKeySecret
        p.setRazorpayPaymentId(req.getRazorpayPaymentId());
        p.setRazorpaySignature(req.getRazorpaySignature());
        p.setStatus(Payment.PaymentStatus.SUCCESS);
        return toResponse(paymentRepository.save(p));
    }

    // ── Refund request ────────────────────────────────────────────────────────

    @Transactional
    public PaymentDto.Response requestRefund(Long userId, PaymentDto.RefundRequest req) {
        Payment p = paymentRepository.findById(req.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (!p.getUser().getId().equals(userId))
            throw new IllegalArgumentException("Not authorized");
        if (!Payment.PaymentStatus.SUCCESS.equals(p.getStatus()))
            throw new IllegalArgumentException("Only successful payments can be refunded");
        if (!"NONE".equals(p.getRefundStatus()))
            throw new IllegalArgumentException("Refund already submitted for this payment");

        long daysSince    = ChronoUnit.DAYS.between(p.getCreatedAt(), LocalDateTime.now());
        BigDecimal refAmt = calculateRefundAmount(p, req.getReason(), daysSince);
        BigDecimal canFee = p.getAmount().subtract(refAmt).max(BigDecimal.ZERO);

        p.setRefundAmount(refAmt.max(BigDecimal.ZERO));
        p.setCancellationFee(canFee);
        p.setRefundStatus(refAmt.compareTo(BigDecimal.ZERO) > 0 ? "REQUESTED" : "REJECTED");
        p.setRefundReason(req.getReason() + (req.getDetails() != null ? ": " + req.getDetails() : ""));
        p.setRefundRequestedAt(LocalDateTime.now());
        p = paymentRepository.save(p);

        // Auto-process via Razorpay for real payments
        if (p.getRazorpayPaymentId() != null
                && !p.getRazorpayPaymentId().startsWith("pay_dev_")
                && refAmt.compareTo(BigDecimal.ZERO) > 0) {
            processRazorpayRefund(p, refAmt);
        }
        return toResponse(p);
    }

    // ── Admin process refund ──────────────────────────────────────────────────

    @Transactional
    public PaymentDto.Response adminProcessRefund(Long paymentId, boolean approve) {
        Payment p = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
        if (approve) {
            p.setRefundStatus("PROCESSED");
            p.setRefundProcessedAt(LocalDateTime.now());
            p.setStatus(p.getRefundAmount() != null
                    && p.getRefundAmount().compareTo(p.getAmount()) >= 0
                    ? Payment.PaymentStatus.REFUNDED
                    : Payment.PaymentStatus.PARTIALLY_REFUNDED);
        } else {
            p.setRefundStatus("REJECTED");
        }
        return toResponse(paymentRepository.save(p));
    }

    // ── Queries ───────────────────────────────────────────────────────────────

    public List<PaymentDto.Response> getPendingRefunds() {
        return paymentRepository.findPendingRefunds().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<PaymentDto.Response> getMyPayments(Long userId) {
        User user = getUser(userId);
        return paymentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public boolean hasActiveUnlimitedPlan(Long userId) {
        User user = getUser(userId);
        LocalDateTime validAfter = LocalDateTime.now().minusDays(180);
        return !paymentRepository.findActiveUnlimitedPlan(user ,validAfter).isEmpty();
    }

    // ── Razorpay refund API ────────────────────────────────────────────────────

    private void processRazorpayRefund(Payment p, BigDecimal refAmt) {
        try {
            String creds   = razorpayKeyId + ":" + razorpayKeySecret;
            String encoded = Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
            HttpHeaders h  = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.set("Authorization", "Basic " + encoded);

            Map<String,Object> body = new LinkedHashMap<>();
            body.put("amount", refAmt.multiply(new BigDecimal("100")).intValue()); // paise
            body.put("notes", Map.of("reason", p.getRefundReason()));

            ResponseEntity<Map> resp = restTemplate.postForEntity(
                    "https://api.razorpay.com/v1/payments/" + p.getRazorpayPaymentId() + "/refund",
                    new HttpEntity<>(body, h), Map.class);

            if (resp.getBody() != null && resp.getBody().get("id") != null) {
                p.setRazorpayRefundId(resp.getBody().get("id").toString());
                p.setRefundStatus("PROCESSED");
                p.setRefundProcessedAt(LocalDateTime.now());
                p.setStatus(refAmt.compareTo(p.getAmount()) >= 0
                        ? Payment.PaymentStatus.REFUNDED
                        : Payment.PaymentStatus.PARTIALLY_REFUNDED);
                paymentRepository.save(p);
            }
        } catch (Exception ignored) { /* kept as REQUESTED for manual admin review */ }
    }

    // ── Refund rule matrix ─────────────────────────────────────────────────────

    private BigDecimal calculateRefundAmount(Payment p, String reason, long daysSince) {
        BigDecimal amt  = p.getAmount();
        Payment.PaymentType type = p.getType();

        if (Payment.PaymentType.DEMO_CLASS_SINGLE.equals(type))
            return "DEMO_UNSATISFIED".equals(reason) ? amt : BigDecimal.ZERO;

        if (Payment.PaymentType.STUDENT_UNLIMITED_PLAN.equals(type)) {
            if (daysSince <= 7)  return amt;
            if (daysSince <= 30) return pct(amt, 70);
            return BigDecimal.ZERO;
        }
        if (Payment.PaymentType.TEACHER_SUBSCRIPTION.equals(type)) {
            if ("NO_LEADS".equals(reason) && daysSince <= 35) return pct(amt, 50);
            return BigDecimal.ZERO;
        }
        if (Payment.PaymentType.TEACHER_ELITE.equals(type))
            return BigDecimal.ZERO;

        // Generic fallback
        if (daysSince <= 7)  return amt;
        if (daysSince <= 30) return pct(amt, 70);
        return BigDecimal.ZERO;
    }

    private BigDecimal pct(BigDecimal amount, int percent) {
        return amount.multiply(new BigDecimal(percent))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private String getRefundPolicyText(Payment.PaymentType type) {
        return switch (type) {
            case DEMO_CLASS_SINGLE      -> "₹10 is refundable ONLY if you are unsatisfied with the demo experience. Non-refundable for no-shows.";
            case STUDENT_UNLIMITED_PLAN -> "Full refund ≤7 days. 30% cancellation fee ≤30 days. No refund after 30 days.";
            case TEACHER_SUBSCRIPTION   -> "50% refund if zero leads in 30 days. We do not guarantee lead volume.";
            case TEACHER_ELITE          -> "Non-refundable after activation.";
            default                     -> "Full refund ≤7 days. 30% cancellation fee ≤30 days. No refund after 30 days.";
        };
    }

    private String getPlanDescription(Payment.PaymentType type) {
        return switch (type) {
            case DEMO_CLASS_SINGLE      -> "Single Demo Class — ₹10";
            case STUDENT_UNLIMITED_PLAN -> "Unlimited Demo Plan (6 months) — ₹49";
            case TEACHER_SUBSCRIPTION   -> "Teacher Basic Plan — ₹499/month";
            case TEACHER_ELITE          -> "Teacher Elite Plan — ₹999";
            default                     -> "EduConnect Platform Fee";
        };
    }

    private PaymentDto.Response toResponse(Payment p) {
        return PaymentDto.Response.builder()
                .id(p.getId())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .razorpayRefundId(p.getRazorpayRefundId())
                .amount(p.getAmount())
                .refundAmount(p.getRefundAmount())
                .status(p.getStatus() != null ? p.getStatus().name() : "CREATED")
                .type(p.getType() != null ? p.getType().name() : "")
                .refundStatus(p.getRefundStatus() != null ? p.getRefundStatus() : "NONE")
                .refundReason(p.getRefundReason())
                .createdAt(p.getCreatedAt())
                .refundRequestedAt(p.getRefundRequestedAt())
                .refundProcessedAt(p.getRefundProcessedAt())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}