package com.unicorn.backend.admin;

import com.unicorn.backend.payment.Payment;
import com.unicorn.backend.payment.PaymentService;
import com.unicorn.backend.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for financial reports and analytics.
 */
@RestController
@RequestMapping("/api/v1/admin/financials")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class FinancialController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    /**
     * Get monthly revenue data for charts.
     * 
     * GET /api/v1/admin/financials/revenue-chart
     */
    @GetMapping("/revenue-chart")
    public ResponseEntity<List<Map<String, Object>>> getRevenueChart() {
        List<Map<String, Object>> revenueData = paymentService.getMonthlyRevenueData();
        return ResponseEntity.ok(revenueData);
    }

    /**
     * Get subscription statistics.
     * 
     * GET /api/v1/admin/financials/subscriptions
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<Map<String, Object>> getSubscriptionStats() {
        Map<String, Object> stats = subscriptionService.getSubscriptionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent payments.
     * 
     * GET /api/v1/admin/financials/payments
     */
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getRecentPayments(
            @RequestParam(defaultValue = "10") int limit) {
        Page<Payment> payments = paymentService.getRecentPayments(limit);
        List<PaymentResponse> response = payments.stream()
                .map(this::toPaymentResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Get financial summary.
     * 
     * GET /api/v1/admin/financials/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getFinancialSummary() {
        Map<String, Object> summary = new HashMap<>();

        // Current month revenue
        BigDecimal currentMonthRevenue = paymentService.getCurrentMonthRevenue();
        summary.put("currentMonthRevenue", currentMonthRevenue);

        // MRR
        BigDecimal mrr = subscriptionService.calculateMRR();
        summary.put("mrr", mrr);

        // Subscription stats
        Map<String, Object> subscriptionStats = subscriptionService.getSubscriptionStats();
        summary.put("subscriptions", subscriptionStats);

        return ResponseEntity.ok(summary);
    }

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .transactionId(payment.getTransactionId())
                .userEmail(payment.getUser().getEmail())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus().name())
                .description(payment.getDescription())
                .paymentMethod(payment.getPaymentMethod())
                .timestamp(payment.getTimestamp())
                .build();
    }
}
