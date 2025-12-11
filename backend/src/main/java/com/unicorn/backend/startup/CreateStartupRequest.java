package com.unicorn.backend.startup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new startup.
 */
public record CreateStartupRequest(
        @NotBlank(message = "Name is required") String name,

        String tagline,

        String fullDescription,

        String industry,

        @NotNull(message = "Stage is required") Stage stage,

        BigDecimal fundingGoal,

        String websiteUrl,

        String logoUrl,

        String pitchDeckUrl,

        String financialDocumentsUrl) {
}
