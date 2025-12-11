package com.unicorn.backend.startup;

import java.math.BigDecimal;

/**
 * Request DTO for updating an existing startup.
 * All fields are optional for partial updates.
 */
public record UpdateStartupRequest(
        String name,

        String tagline,

        String fullDescription,

        String industry,

        Stage stage,

        BigDecimal fundingGoal,

        BigDecimal raisedAmount,

        String websiteUrl,

        String logoUrl,

        String pitchDeckUrl,

        String financialDocumentsUrl) {
}
