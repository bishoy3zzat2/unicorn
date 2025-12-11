package com.unicorn.backend.startup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Startup entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartupResponse {
    private UUID id;
    private String name;
    private String tagline;
    private String fullDescription;
    private String industry;
    private Stage stage;
    private BigDecimal fundingGoal;
    private BigDecimal raisedAmount;
    private String websiteUrl;
    private String logoUrl;
    private String pitchDeckUrl;
    private String financialDocumentsUrl;
    private StartupStatus status;
    private UUID ownerId;
    private String ownerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts a Startup entity to a StartupResponse DTO.
     *
     * @param startup the entity to convert
     * @return the response DTO
     */
    public static StartupResponse fromEntity(Startup startup) {
        return StartupResponse.builder()
                .id(startup.getId())
                .name(startup.getName())
                .tagline(startup.getTagline())
                .fullDescription(startup.getFullDescription())
                .industry(startup.getIndustry())
                .stage(startup.getStage())
                .fundingGoal(startup.getFundingGoal())
                .raisedAmount(startup.getRaisedAmount())
                .websiteUrl(startup.getWebsiteUrl())
                .logoUrl(startup.getLogoUrl())
                .pitchDeckUrl(startup.getPitchDeckUrl())
                .financialDocumentsUrl(startup.getFinancialDocumentsUrl())
                .status(startup.getStatus())
                .ownerId(startup.getOwner().getId())
                .ownerEmail(startup.getOwner().getEmail())
                .createdAt(startup.getCreatedAt())
                .updatedAt(startup.getUpdatedAt())
                .build();
    }
}
