package com.unicorn.backend.deal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for creating or updating a deal.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealRequest {

    @NotNull(message = "Investor ID is required")
    private UUID investorId;

    @NotNull(message = "Startup ID is required")
    private UUID startupId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    @Builder.Default
    private DealStatus status = DealStatus.PENDING;

    private DealType dealType;

    private BigDecimal equityPercentage;

    private BigDecimal commissionPercentage;

    private String notes;

    private LocalDateTime dealDate;
}
