package com.unicorn.backend.startup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for creating a new startup.
 */
public record CreateStartupRequest(
        @NotBlank(message = "Name is required") String name,

        String tagline,

        String fullDescription,

        @NotNull(message = "Industry is required") String industry,

        @NotNull(message = "Stage is required") Stage stage,

        @NotNull(message = "Funding goal is required") @Positive(message = "Funding goal must be positive") BigDecimal fundingGoal,

        String websiteUrl,

        String logoUrl,

        String coverUrl,

        String facebookUrl,

        String instagramUrl,

        String twitterUrl,

        String pitchDeckUrl,

        String businessPlanUrl,

        String businessModelUrl,

        String financialDocumentsUrl,

        UUID ownerId, // Optional: Only used by admins to assign ownership

        @NotNull(message = "Owner role is required") StartupRole ownerRole) {
}
