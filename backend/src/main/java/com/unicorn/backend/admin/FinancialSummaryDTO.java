package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for financial summary statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryDTO {

    // Revenue metrics
    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private BigDecimal revenueGrowthPercent;
    private BigDecimal mrr; // Monthly Recurring Revenue
    private BigDecimal arr; // Annual Recurring Revenue
    private BigDecimal totalLifetimeRevenue;
    private BigDecimal arpu; // Average Revenue Per User

    // Subscription metrics
    private long totalUsers;
    private long freeUsers;
    private long proSubscribers;
    private long eliteSubscribers;
    private long activeSubscriptions;
    private BigDecimal conversionRate; // Free to paid conversion
    private BigDecimal churnRate;

    // Payment metrics
    private long totalPayments;
    private long completedPayments;
    private long pendingPayments;
    private long failedPayments;
    private long refundedPayments;

    // Deals metrics (commission)
    private BigDecimal totalCommissionRevenue;
    private long totalDeals;
    private long completedDeals;
}
