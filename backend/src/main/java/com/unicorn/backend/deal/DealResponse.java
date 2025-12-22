package com.unicorn.backend.deal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for returning deal data with related entity information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealResponse {

    private String id;

    // Investor info
    private String investorId;
    private String investorName;
    private String investorEmail;
    private String investorAvatar;

    // Startup info
    private String startupId;
    private String startupName;
    private String startupLogo;

    // Deal details
    private BigDecimal amount;
    private String currency;
    private String status;
    private String dealType;
    private BigDecimal equityPercentage;
    private BigDecimal commissionPercentage;
    private String notes;
    private LocalDateTime dealDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Factory method to create DealResponse from Deal entity.
     */
    public static DealResponse fromEntity(Deal deal) {
        String investorName = "";
        if (deal.getInvestor() != null) {
            String firstName = deal.getInvestor().getFirstName();
            String lastName = deal.getInvestor().getLastName();
            if (firstName != null || lastName != null) {
                investorName = ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
            }
            if (investorName.isEmpty()) {
                investorName = deal.getInvestor().getEmail();
            }
        }

        return DealResponse.builder()
                .id(deal.getId().toString())
                .investorId(deal.getInvestor() != null ? deal.getInvestor().getId().toString() : null)
                .investorName(investorName)
                .investorEmail(deal.getInvestor() != null ? deal.getInvestor().getEmail() : null)
                .investorAvatar(deal.getInvestor() != null ? deal.getInvestor().getAvatarUrl() : null)
                .startupId(deal.getStartup() != null ? deal.getStartup().getId().toString() : null)
                .startupName(deal.getStartup() != null ? deal.getStartup().getName() : null)
                .startupLogo(deal.getStartup() != null ? deal.getStartup().getLogoUrl() : null)
                .amount(deal.getAmount())
                .currency(deal.getCurrency())
                .status(deal.getStatus() != null ? deal.getStatus().name() : null)
                .dealType(deal.getDealType() != null ? deal.getDealType().name() : null)
                .equityPercentage(deal.getEquityPercentage())
                .commissionPercentage(deal.getCommissionPercentage())
                .notes(deal.getNotes())
                .dealDate(deal.getDealDate())
                .createdAt(deal.getCreatedAt())
                .updatedAt(deal.getUpdatedAt())
                .build();
    }
}
