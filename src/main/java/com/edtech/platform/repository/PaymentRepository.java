package com.edtech.platform.repository;

import com.edtech.platform.entity.Payment;
import com.edtech.platform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByRazorpayOrderId(String orderId);
    List<Payment> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.status = 'SUCCESS'")
    BigDecimal totalRevenue();

    List<Payment> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.status = 'SUCCESS' AND p.paymentMode = 'CASH'")
    BigDecimal totalCashRevenue();
}