package com.unicorn.backend.deal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for deal statistics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealStats {

    private long totalDeals;
    private long pendingDeals;
    private long completedDeals;
    private long cancelledDeals;
    private BigDecimal totalCompletedAmount;
}
