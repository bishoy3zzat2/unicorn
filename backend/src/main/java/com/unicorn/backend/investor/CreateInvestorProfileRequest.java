package com.unicorn.backend.investor;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating an investor profile.
 */
public record CreateInvestorProfileRequest(
        @NotNull(message = "Investment budget is required") BigDecimal investmentBudget,

        String bio,

        String preferredIndustries,

        String linkedInUrl) {
}
