package com.unicorn.backend.admin;

import com.unicorn.backend.appconfig.AppConfigService;
import com.unicorn.backend.deal.DealService;
import com.unicorn.backend.deal.DealStatus;
import com.unicorn.backend.payment.Payment;
import com.unicorn.backend.payment.PaymentRepository;
import com.unicorn.backend.payment.PaymentStatus;
import com.unicorn.backend.subscription.SubscriptionPlan;
import com.unicorn.backend.subscription.SubscriptionRepository;
import com.unicorn.backend.subscription.SubscriptionStatus;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for financial analytics and dashboard data.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/financials")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class FinancialsController {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final DealService dealService;
    private final AppConfigService appConfigService;

    /**
     * Get comprehensive financial summary with USD currency conversion.
     */
    @GetMapping("/summary")
    public ResponseEntity<FinancialSummaryDTO> getFinancialSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime startOfPrevMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfPrevMonth = startOfMonth.minusSeconds(1);

        // Revenue calculations (Converted to USD)
        BigDecimal currentMonthRevenue = calculateRevenueForPeriod(startOfMonth, now);
        BigDecimal previousMonthRevenue = calculateRevenueForPeriod(startOfPrevMonth, endOfPrevMonth);

        BigDecimal revenueGrowth = BigDecimal.ZERO;
        if (previousMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowth = currentMonthRevenue.subtract(previousMonthRevenue)
                    .divide(previousMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Calculate MRR (Converted to USD)
        BigDecimal mrr = calculateMRR();
        BigDecimal arr = mrr.multiply(BigDecimal.valueOf(12));

        // Get subscription counts for display
        long proSubs = subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.PRO, SubscriptionStatus.ACTIVE);
        long eliteSubs = subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.ELITE,
                SubscriptionStatus.ACTIVE);

        // User metrics
        long totalUsers = userRepository.count();
        long activeSubscriptions = proSubs + eliteSubs;
        long freeUsers = totalUsers - activeSubscriptions;

        // ARPU calculation
        BigDecimal arpu = BigDecimal.ZERO;
        if (activeSubscriptions > 0) {
            arpu = mrr.divide(BigDecimal.valueOf(activeSubscriptions), 2, RoundingMode.HALF_UP);
        }

        // Conversion rate (paid / total)
        BigDecimal conversionRate = BigDecimal.ZERO;
        if (totalUsers > 0) {
            conversionRate = BigDecimal.valueOf(activeSubscriptions)
                    .divide(BigDecimal.valueOf(totalUsers), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Payment metrics
        long completedPayments = paymentRepository.countByStatus(PaymentStatus.COMPLETED);
        long pendingPayments = paymentRepository.countByStatus(PaymentStatus.PENDING);
        long failedPayments = paymentRepository.countByStatus(PaymentStatus.FAILED);
        long refundedPayments = paymentRepository.countByStatus(PaymentStatus.REFUNDED);
        long totalPayments = completedPayments + pendingPayments + failedPayments + refundedPayments;

        // Lifetime revenue (Converted to USD)
        BigDecimal lifetimeRevenue = calculateRevenueForPeriod(
                LocalDateTime.of(2020, 1, 1, 0, 0), now);

        // Deals/Commission metrics (Already converted to USD in DealService)
        BigDecimal totalCommission = dealService.getDealStats().getTotalCommissionRevenue();
        long totalDeals = dealService.getDealStats().getTotalDeals();
        long completedDeals = dealService.getDealStats().getCompletedDeals();

        FinancialSummaryDTO summary = FinancialSummaryDTO.builder()
                .currentMonthRevenue(currentMonthRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .revenueGrowthPercent(revenueGrowth)
                .mrr(mrr)
                .arr(arr)
                .totalLifetimeRevenue(lifetimeRevenue)
                .arpu(arpu)
                .totalUsers(totalUsers)
                .freeUsers(freeUsers)
                .proSubscribers(proSubs)
                .eliteSubscribers(eliteSubs)
                .activeSubscriptions(activeSubscriptions)
                .conversionRate(conversionRate)
                .churnRate(BigDecimal.ZERO)
                .totalPayments(totalPayments)
                .completedPayments(completedPayments)
                .pendingPayments(pendingPayments)
                .failedPayments(failedPayments)
                .refundedPayments(refundedPayments)
                .totalCommissionRevenue(totalCommission != null ? totalCommission : BigDecimal.ZERO)
                .totalDeals(totalDeals)
                .completedDeals(completedDeals)
                .build();

        return ResponseEntity.ok(summary);
    }

    /**
     * Get monthly revenue data for charts (last 12 months) with USD conversion.
     */
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<RevenueDataPointDTO>> getMonthlyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusMonths(11).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rawData = paymentRepository.getMonthlyRevenueByCurrency(startDate);

        // Map: MonthNum -> Total Revenue (USD)
        Map<Integer, BigDecimal> monthlyRevenueMap = new HashMap<>();
        Map<Integer, String> monthNameMap = new HashMap<>();

        for (Object[] row : rawData) {
            String monthName = (String) row[0];
            int monthNum = ((Number) row[1]).intValue();
            String currency = (String) row[2];
            BigDecimal amount = (BigDecimal) row[3];

            BigDecimal amountUSD = convertToUSD(amount, currency);

            monthlyRevenueMap.put(monthNum, monthlyRevenueMap.getOrDefault(monthNum, BigDecimal.ZERO).add(amountUSD));
            monthNameMap.put(monthNum, monthName);
        }

        List<RevenueDataPointDTO> result = new ArrayList<>();

        // Ensure all 12 months are present, even if 0 revenue
        for (int i = 0; i < 12; i++) {
            LocalDateTime monthDate = startDate.plusMonths(i);
            int monthNum = monthDate.getMonthValue();
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            result.add(RevenueDataPointDTO.builder()
                    .month(monthName)
                    .monthNum(monthNum)
                    .revenue(monthlyRevenueMap.getOrDefault(monthNum, BigDecimal.ZERO))
                    .proRevenue(BigDecimal.ZERO) // Simplified
                    .eliteRevenue(BigDecimal.ZERO)
                    .build());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get daily revenue for last 30 days with USD conversion.
     */
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<Map<String, Object>>> getDailyRevenue() {
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 29; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);

            BigDecimal revenue = calculateRevenueForPeriod(dayStart, dayEnd);

            Map<String, Object> point = new HashMap<>();
            point.put("date", dayStart.toLocalDate().toString());
            point.put("day", dayStart.getDayOfMonth());
            point.put("revenue", revenue);
            result.add(point);
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Get subscription statistics breakdown with USD conversion.
     */
    @GetMapping("/subscriptions/stats")
    public ResponseEntity<SubscriptionStatsDTO> getSubscriptionStats() {
        long totalSubs = subscriptionRepository.count();
        long activeSubs = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long cancelledSubs = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);
        long expiredSubs = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);

        long proActive = subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.PRO,
                SubscriptionStatus.ACTIVE);
        long eliteActive = subscriptionRepository.countByPlanTypeAndStatus(SubscriptionPlan.ELITE,
                SubscriptionStatus.ACTIVE);

        long freeUsers = userRepository.count() - proActive - eliteActive;

        // Calculate revenue by plan with USD conversion
        BigDecimal proRevenue = calculateMRRByPlan(SubscriptionPlan.PRO);
        BigDecimal eliteRevenue = calculateMRRByPlan(SubscriptionPlan.ELITE);

        SubscriptionStatsDTO stats = SubscriptionStatsDTO.builder()
                .totalSubscriptions(totalSubs)
                .activeSubscriptions(activeSubs)
                .cancelledSubscriptions(cancelledSubs)
                .expiredSubscriptions(expiredSubs)
                .freeUsers(freeUsers)
                .proMonthly(proActive)
                .proYearly(0)
                .eliteMonthly(eliteActive)
                .eliteYearly(0)
                .proRevenue(proRevenue)
                .eliteRevenue(eliteRevenue)
                .totalRevenue(proRevenue.add(eliteRevenue))
                .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Get recent payments with pagination.
     */
    @GetMapping("/payments")
    public ResponseEntity<Page<PaymentResponse>> getPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Payment> payments = paymentRepository.findAllByOrderByTimestampDesc(pageable);

        Page<PaymentResponse> response = payments.map(p -> PaymentResponse.builder()
                .transactionId(p.getTransactionId())
                .userEmail(p.getUser().getEmail())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus().name())
                .description(p.getDescription())
                .paymentMethod(p.getPaymentMethod())
                .timestamp(p.getTimestamp())
                .build());

        return ResponseEntity.ok(response);
    }

    /**
     * Get payment status breakdown for pie chart.
     */
    @GetMapping("/payments/status-breakdown")
    public ResponseEntity<Map<String, Long>> getPaymentStatusBreakdown() {
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("COMPLETED", paymentRepository.countByStatus(PaymentStatus.COMPLETED));
        breakdown.put("PENDING", paymentRepository.countByStatus(PaymentStatus.PENDING));
        breakdown.put("FAILED", paymentRepository.countByStatus(PaymentStatus.FAILED));
        breakdown.put("REFUNDED", paymentRepository.countByStatus(PaymentStatus.REFUNDED));
        return ResponseEntity.ok(breakdown);
    }

    // --- Helper Methods for Currency Conversion ---

    private BigDecimal calculateRevenueForPeriod(LocalDateTime start, LocalDateTime end) {
        List<Object[]> results = paymentRepository.getTotalRevenueForPeriodByCurrency(start, end);
        BigDecimal totalUSD = BigDecimal.ZERO;
        for (Object[] row : results) {
            String currency = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totalUSD = totalUSD.add(convertToUSD(amount, currency));
        }
        return totalUSD;
    }

    private BigDecimal calculateMRR() {
        List<Object[]> results = subscriptionRepository.calculateTotalActiveMRRByCurrency();
        BigDecimal totalUSD = BigDecimal.ZERO;
        for (Object[] row : results) {
            String currency = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totalUSD = totalUSD.add(convertToUSD(amount, currency));
        }
        return totalUSD;
    }

    private BigDecimal calculateMRRByPlan(SubscriptionPlan plan) {
        List<Object[]> results = subscriptionRepository.calculateMRRByPlanAndCurrency(plan);
        BigDecimal totalUSD = BigDecimal.ZERO;
        for (Object[] row : results) {
            String currency = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            totalUSD = totalUSD.add(convertToUSD(amount, currency));
        }
        return totalUSD;
    }

    private BigDecimal convertToUSD(BigDecimal amount, String currency) {
        if (amount == null)
            return BigDecimal.ZERO;
        if (currency == null || "USD".equalsIgnoreCase(currency)) {
            return amount;
        }

        String rateKey = "rate_" + currency.toLowerCase();
        String rateValue = appConfigService.getValue(rateKey).orElse(null);

        if (rateValue != null) {
            try {
                BigDecimal rate = new BigDecimal(rateValue);
                // Rate format: 1 USD = X Currency => USD Amount = Currency Amount / Rate
                return amount.divide(rate, 2, RoundingMode.HALF_UP);
            } catch (NumberFormatException e) {
                return amount;
            }
        }
        return amount;
    }
}
