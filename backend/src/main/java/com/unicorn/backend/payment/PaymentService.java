package com.unicorn.backend.payment;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing payment transactions.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    /**
     * Create a new payment record.
     */
    @Transactional
    public Payment createPayment(UUID userId, BigDecimal amount, String description, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Payment payment = Payment.builder()
                .transactionId(generateTransactionId())
                .user(user)
                .amount(amount)
                .currency("EGP")
                .status(PaymentStatus.PENDING)
                .description(description)
                .paymentMethod(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * Mark payment as completed.
     */
    @Transactional
    public Payment completePayment(String transactionId) {
        Payment payment = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }

    /**
     * Mark payment as failed.
     */
    @Transactional
    public Payment failPayment(String transactionId) {
        Payment payment = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    /**
     * Get recent payments.
     */
    public Page<Payment> getRecentPayments(int limit) {
        return paymentRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    /**
     * Get payments for a user.
     */
    public Page<Payment> getPaymentsForUser(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get monthly revenue data for charts.
     */
    public List<Map<String, Object>> getMonthlyRevenueData() {
        LocalDateTime startOfYear = LocalDateTime.now().withMonth(1).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rawData = paymentRepository.getMonthlyRevenue(startOfYear);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", row[0]);
            monthData.put("revenue", row[2]);
            result.add(monthData);
        }

        return result;
    }

    /**
     * Get total revenue for current month.
     */
    public BigDecimal getCurrentMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        BigDecimal revenue = paymentRepository.getTotalRevenueForPeriod(startOfMonth, endOfMonth);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Generate unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
