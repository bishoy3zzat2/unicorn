package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for revenue chart data point.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDataPointDTO {
    private String month;
    private int monthNum;
    private BigDecimal revenue;
    private BigDecimal proRevenue;
    private BigDecimal eliteRevenue;
}
