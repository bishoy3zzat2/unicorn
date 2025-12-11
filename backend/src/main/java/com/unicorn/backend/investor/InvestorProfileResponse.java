package com.unicorn.backend.investor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for InvestorProfile entity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestorProfileResponse {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private BigDecimal investmentBudget;
    private String bio;
    private String preferredIndustries;
    private String linkedInUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Converts an InvestorProfile entity to a response DTO.
     *
     * @param profile the entity to convert
     * @return the response DTO
     */
    public static InvestorProfileResponse fromEntity(InvestorProfile profile) {
        return InvestorProfileResponse.builder()
                .id(profile.getId())
                .userId(profile.getUser().getId())
                .userEmail(profile.getUser().getEmail())
                .investmentBudget(profile.getInvestmentBudget())
                .bio(profile.getBio())
                .preferredIndustries(profile.getPreferredIndustries())
                .linkedInUrl(profile.getLinkedInUrl())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
