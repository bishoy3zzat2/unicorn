package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for dashboard statistics response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalUsers;
    private long activeStartups;
    private long activeInvestors;
    private BigDecimal mrr;
    private long pendingVerifications;
    private BigDecimal totalFunding;

    // Growth percentages (compared to last month)
    private double userGrowth;
    private double startupGrowth;
    private double investorGrowth;
    private double mrrGrowth;
}
