package com.edtech.platform.repository;

import com.edtech.platform.entity.Payment;
import com.edtech.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByRazorpayOrderId(String orderId);

    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    List<Payment> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal totalRevenue();

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.paymentMode = 'CASH'")
    BigDecimal totalCashRevenue();

    /** All refund requests pending admin review */
    @Query("SELECT p FROM Payment p WHERE p.refundStatus = 'REQUESTED' ORDER BY p.refundRequestedAt ASC")
    List<Payment> findPendingRefunds();

    /**
     * Check if a user has an active unlimited student plan.
     * cutoffDate = LocalDateTime.now().minusDays(180) — passed from service to avoid HQL date arithmetic.
     */
    @Query("""
        SELECT p FROM Payment p
        WHERE p.user       = :user
          AND p.type       = 'STUDENT_UNLIMITED_PLAN'
          AND p.status     = 'SUCCESS'
          AND p.createdAt >= :cutoffDate
        ORDER BY p.createdAt DESC
        """)
    List<Payment> findActiveUnlimitedPlan(
            @Param("user")       User user,
            @Param("cutoffDate") LocalDateTime cutoffDate);
}