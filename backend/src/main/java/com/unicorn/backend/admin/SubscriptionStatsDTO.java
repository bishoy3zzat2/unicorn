package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for subscription statistics breakdown.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatsDTO {
    private long totalSubscriptions;
    private long activeSubscriptions;
    private long cancelledSubscriptions;
    private long expiredSubscriptions;

    // By plan breakdown
    private long freeUsers;
    private long proMonthly;
    private long proYearly;
    private long eliteMonthly;
    private long eliteYearly;

    // Revenue by plan
    private BigDecimal proRevenue;
    private BigDecimal eliteRevenue;
    private BigDecimal totalRevenue;
}
