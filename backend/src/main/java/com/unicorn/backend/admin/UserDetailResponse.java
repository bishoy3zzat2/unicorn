package com.unicorn.backend.admin;

import com.unicorn.backend.investor.InvestorProfile;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupResponse;
import com.unicorn.backend.subscription.Subscription;
import com.unicorn.backend.payment.Payment;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserModerationLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Comprehensive user details response for admin view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailResponse {
    // Basic Info
    private UUID id;
    private String email;
    private String displayName;
    private String phoneNumber;
    private String country;
    private String role;
    private String status;
    private String authProvider;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;

    // Suspension info
    private LocalDateTime suspendedAt;
    private String suspendReason;
    private LocalDateTime suspendedUntil;
    private String suspensionType;

    // Deletion info
    private LocalDateTime deletedAt;
    private String deletionReason;

    // Stats
    private long warningCount;
    private int startupCount;
    private boolean hasInvestorProfile;
    private boolean isInvestorVerified;

    // Subscription Info
    private SubscriptionInfo currentSubscription;

    // Investor Profile Info (if applicable)
    private InvestorInfo investorInfo;

    // Startups owned (simplified list)
    private List<StartupSummary> startups;

    // Recent Transactions
    private List<TransactionInfo> recentTransactions;

    // Moderation history
    private List<ModerationLogResponse> moderationHistory;

    // Nested DTOs
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SubscriptionInfo {
        private String plan;
        private String status;
        private BigDecimal amount;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvestorInfo {
        private UUID id;
        private String bio;
        private BigDecimal investmentBudget;
        private String preferredIndustries;
        private String linkedInUrl;
        private boolean isVerified;
        private LocalDateTime verifiedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StartupSummary {
        private UUID id;
        private String name;
        private String industry;
        private String stage;
        private String status;
        private BigDecimal raisedAmount;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionInfo {
        private String transactionId;
        private BigDecimal amount;
        private String currency;
        private String status;
        private String description;
        private String paymentMethod;
        private LocalDateTime timestamp;
    }

    public static UserDetailResponse fromEntity(
            User user,
            List<UserModerationLog> history,
            long warningCount,
            Subscription currentSubscription,
            List<Payment> recentPayments) {

        UserDetailResponseBuilder builder = UserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .phoneNumber(user.getPhoneNumber())
                .country(user.getCountry())
                .role(user.getRole())
                .status(user.getStatus())
                .authProvider(user.getAuthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .passwordChangedAt(user.getPasswordChangedAt())
                .suspendedAt(user.getSuspendedAt())
                .suspendReason(user.getSuspendReason())
                .suspendedUntil(user.getSuspendedUntil())
                .suspensionType(user.getSuspensionType())
                .deletedAt(user.getDeletedAt())
                .deletionReason(user.getDeletionReason())
                .warningCount(warningCount)
                .startupCount(user.getStartups() != null ? user.getStartups().size() : 0)
                .hasInvestorProfile(user.getInvestorProfile() != null)
                .isInvestorVerified(user.getInvestorProfile() != null &&
                        Boolean.TRUE.equals(user.getInvestorProfile().getIsVerified()))
                .moderationHistory(history.stream()
                        .map(ModerationLogResponse::fromEntity)
                        .toList());

        // Add subscription info
        if (currentSubscription != null) {
            builder.currentSubscription(SubscriptionInfo.builder()
                    .plan(currentSubscription.getPlanType().name())
                    .status(currentSubscription.getStatus().name())
                    .amount(currentSubscription.getAmount())
                    .startDate(currentSubscription.getStartDate())
                    .endDate(currentSubscription.getEndDate())
                    .build());
        } else {
            builder.currentSubscription(SubscriptionInfo.builder()
                    .plan("FREE")
                    .status("ACTIVE")
                    .amount(BigDecimal.ZERO)
                    .build());
        }

        // Add investor info
        InvestorProfile investorProfile = user.getInvestorProfile();
        if (investorProfile != null) {
            builder.investorInfo(InvestorInfo.builder()
                    .id(investorProfile.getId())
                    .bio(investorProfile.getBio())
                    .investmentBudget(investorProfile.getInvestmentBudget())
                    .preferredIndustries(investorProfile.getPreferredIndustries())
                    .linkedInUrl(investorProfile.getLinkedInUrl())
                    .isVerified(Boolean.TRUE.equals(investorProfile.getIsVerified()))
                    .verifiedAt(investorProfile.getVerifiedAt())
                    .build());
        }

        // Add startups
        if (user.getStartups() != null && !user.getStartups().isEmpty()) {
            builder.startups(user.getStartups().stream()
                    .map(s -> StartupSummary.builder()
                            .id(s.getId())
                            .name(s.getName())
                            .industry(s.getIndustry())
                            .stage(s.getStage() != null ? s.getStage().name() : null)
                            .status(s.getStatus() != null ? s.getStatus().name() : null)
                            .raisedAmount(s.getRaisedAmount())
                            .createdAt(s.getCreatedAt())
                            .build())
                    .toList());
        }

        // Add recent transactions
        if (recentPayments != null && !recentPayments.isEmpty()) {
            builder.recentTransactions(recentPayments.stream()
                    .map(p -> TransactionInfo.builder()
                            .transactionId(p.getTransactionId())
                            .amount(p.getAmount())
                            .currency(p.getCurrency())
                            .status(p.getStatus() != null ? p.getStatus().name() : null)
                            .description(p.getDescription())
                            .paymentMethod(p.getPaymentMethod())
                            .timestamp(p.getTimestamp())
                            .build())
                    .toList());
        }

        return builder.build();
    }
}
