package com.unicorn.backend.nudge;

import com.unicorn.backend.appconfig.AppConfigService;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.subscription.Subscription;
import com.unicorn.backend.subscription.SubscriptionPlan;
import com.unicorn.backend.subscription.SubscriptionService;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing nudge operations with plan-based limits and cooldowns.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NudgeService {

    private final NudgeRepository nudgeRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final SubscriptionService subscriptionService;
    private final AppConfigService appConfigService;
    private final SimpMessagingTemplate messagingTemplate;

    // Default config values
    private static final int DEFAULT_FREE_MONTHLY_LIMIT = 4;
    private static final int DEFAULT_PRO_MONTHLY_LIMIT = 12;
    private static final int DEFAULT_PRO_COOLDOWN_DAYS = 5;
    private static final int DEFAULT_ELITE_COOLDOWN_DAYS = 3;

    /**
     * Check if a sender can nudge a receiver based on plan limits.
     */
    public NudgeAvailabilityResponse canNudge(User sender, User receiver) {
        SubscriptionPlan plan = getUserPlan(sender);
        String planName = plan.name();

        int monthlyLimit = getMonthlyLimit(plan);
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                .withNano(0);
        long usedThisMonth = nudgeRepository.countSentThisMonth(sender, startOfMonth);
        int remaining = Math.max(0, monthlyLimit - (int) usedThisMonth);

        // Check monthly limit (ELITE has unlimited)
        if (plan != SubscriptionPlan.ELITE && usedThisMonth >= monthlyLimit) {
            return NudgeAvailabilityResponse.denied(
                    "Monthly nudge limit reached (" + monthlyLimit + " nudges/month for " + planName + " plan)",
                    planName, remaining, (int) usedThisMonth, monthlyLimit, null);
        }

        // Check per-investor limit based on plan
        switch (plan) {
            case FREE:
                // FREE: Can only nudge each investor once ever
                if (nudgeRepository.existsBySenderAndReceiver(sender, receiver)) {
                    return NudgeAvailabilityResponse.denied(
                            "FREE plan allows only one nudge per investor",
                            planName, remaining, (int) usedThisMonth, monthlyLimit, null);
                }
                break;

            case PRO:
                // PRO: Cooldown of X days per investor
                int proCooldownDays = appConfigService.getIntValue("nudge.cooldown.pro.days",
                        DEFAULT_PRO_COOLDOWN_DAYS);
                Optional<LocalDateTime> proCooldownEnds = checkCooldown(sender, receiver, proCooldownDays);
                if (proCooldownEnds.isPresent()) {
                    return NudgeAvailabilityResponse.denied(
                            "Cooldown active. You can nudge this investor again in " +
                                    ChronoUnit.DAYS.between(LocalDateTime.now(), proCooldownEnds.get()) + " days",
                            planName, remaining, (int) usedThisMonth, monthlyLimit, proCooldownEnds.get());
                }
                break;

            case ELITE:
                // ELITE: Cooldown of X days per investor (no monthly limit)
                int eliteCooldownDays = appConfigService.getIntValue("nudge.cooldown.elite.days",
                        DEFAULT_ELITE_COOLDOWN_DAYS);
                Optional<LocalDateTime> eliteCooldownEnds = checkCooldown(sender, receiver, eliteCooldownDays);
                if (eliteCooldownEnds.isPresent()) {
                    return NudgeAvailabilityResponse.denied(
                            "Cooldown active. You can nudge this investor again in " +
                                    ChronoUnit.DAYS.between(LocalDateTime.now(), eliteCooldownEnds.get()) + " days",
                            planName, remaining, (int) usedThisMonth, monthlyLimit, eliteCooldownEnds.get());
                }
                break;
        }

        return NudgeAvailabilityResponse.allowed(planName, remaining, (int) usedThisMonth, monthlyLimit);
    }

    /**
     * Send a nudge from sender to investor for a specific startup.
     */
    @Transactional
    public Nudge sendNudge(User sender, UUID investorId, UUID startupId) {
        User receiver = userRepository.findById(investorId)
                .orElseThrow(() -> new RuntimeException("Investor not found: " + investorId));

        // Verify receiver is an investor
        if (!"INVESTOR".equals(receiver.getRole())) {
            throw new RuntimeException("Target user is not an investor");
        }

        // Get and verify startup
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new RuntimeException("Startup not found: " + startupId));

        // Verify sender owns the startup
        if (!startup.getOwner().getId().equals(sender.getId())) {
            throw new RuntimeException("You are not the owner of this startup");
        }

        // Check eligibility
        NudgeAvailabilityResponse availability = canNudge(sender, receiver);
        if (!availability.isCanNudge()) {
            throw new RuntimeException(availability.getReason());
        }

        // Create and save nudge
        Nudge nudge = Nudge.builder()
                .sender(sender)
                .receiver(receiver)
                .startup(startup)
                .build();

        nudge = nudgeRepository.save(nudge);
        log.info("Nudge sent from {} to {} for startup {}", sender.getEmail(), receiver.getEmail(), startup.getName());

        // Send real-time notification
        sendNudgeNotification(nudge);

        return nudge;
    }

    /**
     * Get nudge statistics for a user (for admin dashboard).
     */
    public UserNudgeStatsResponse getUserNudgeStats(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String role = user.getRole();

        if ("INVESTOR".equals(role)) {
            List<Nudge> receivedNudges = nudgeRepository.findByReceiverOrderByCreatedAtDesc(user);
            return UserNudgeStatsResponse.builder()
                    .userRole(role)
                    .receivedCount((long) receivedNudges.size())
                    .nudges(receivedNudges.stream()
                            .map(NudgeInfoResponse::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        } else if ("STARTUP_OWNER".equals(role)) {
            SubscriptionPlan plan = getUserPlan(user);
            int monthlyLimit = getMonthlyLimit(plan);
            LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                    .withNano(0);
            long sentThisMonth = nudgeRepository.countSentThisMonth(user, startOfMonth);
            int remaining = plan == SubscriptionPlan.ELITE ? -1 : Math.max(0, monthlyLimit - (int) sentThisMonth);

            List<Nudge> sentNudges = nudgeRepository.findBySenderOrderByCreatedAtDesc(user);
            return UserNudgeStatsResponse.builder()
                    .userRole(role)
                    .sentCount((long) sentNudges.size())
                    .remainingThisMonth(remaining)
                    .monthlyLimit(plan == SubscriptionPlan.ELITE ? -1 : monthlyLimit)
                    .currentPlan(plan.name())
                    .nudges(sentNudges.stream()
                            .map(NudgeInfoResponse::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }

        // For other roles, return empty stats
        return UserNudgeStatsResponse.builder()
                .userRole(role)
                .nudges(List.of())
                .build();
    }

    /**
     * Get nudges sent for a specific startup (for admin dashboard).
     */
    public List<NudgeInfoResponse> getStartupNudges(UUID startupId) {
        List<Nudge> nudges = nudgeRepository.findByStartupIdOrderByCreatedAtDesc(startupId);
        return nudges.stream()
                .map(NudgeInfoResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Count nudges for a specific startup.
     */
    public long countStartupNudges(UUID startupId) {
        return nudgeRepository.countByStartupId(startupId);
    }

    /**
     * Get user's current subscription plan.
     */
    private SubscriptionPlan getUserPlan(User user) {
        Subscription active = subscriptionService.getActiveSubscription(user.getId());
        return active != null ? active.getPlanType() : SubscriptionPlan.FREE;
    }

    /**
     * Get monthly limit based on plan from config.
     */
    private int getMonthlyLimit(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> appConfigService.getIntValue("nudge.limit.free.monthly", DEFAULT_FREE_MONTHLY_LIMIT);
            case PRO -> appConfigService.getIntValue("nudge.limit.pro.monthly", DEFAULT_PRO_MONTHLY_LIMIT);
            case ELITE -> Integer.MAX_VALUE; // Unlimited
        };
    }

    /**
     * Check cooldown period for a sender-receiver pair.
     * Returns the cooldown end time if active, empty if no cooldown.
     */
    private Optional<LocalDateTime> checkCooldown(User sender, User receiver, int cooldownDays) {
        Optional<Nudge> lastNudge = nudgeRepository.findTopBySenderAndReceiverOrderByCreatedAtDesc(sender, receiver);
        if (lastNudge.isEmpty()) {
            return Optional.empty();
        }

        LocalDateTime cooldownEnds = lastNudge.get().getCreatedAt().plusDays(cooldownDays);
        if (LocalDateTime.now().isBefore(cooldownEnds)) {
            return Optional.of(cooldownEnds);
        }

        return Optional.empty();
    }

    /**
     * Send real-time WebSocket notification to the nudge receiver.
     */
    private void sendNudgeNotification(Nudge nudge) {
        try {
            NudgeNotification notification = NudgeNotification.builder()
                    .type("NUDGE")
                    .senderName(nudge.getSender().getFirstName() != null
                            ? nudge.getSender().getFirstName() + " " + nudge.getSender().getLastName()
                            : nudge.getSender().getEmail().split("@")[0])
                    .senderAvatarUrl(nudge.getSender().getAvatarUrl())
                    .startupId(nudge.getStartup().getId().toString())
                    .startupName(nudge.getStartup().getName())
                    .startupLogoUrl(nudge.getStartup().getLogoUrl())
                    .message("wants you to check out " + nudge.getStartup().getName())
                    .timestamp(nudge.getCreatedAt())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    nudge.getReceiver().getUsername(),
                    "/queue/nudge",
                    notification);
            log.debug("Nudge notification sent to user: {}", nudge.getReceiver().getUsername());
        } catch (Exception e) {
            log.error("Failed to send nudge notification: {}", e.getMessage());
        }
    }

    /**
     * Internal DTO for nudge notifications.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class NudgeNotification {
        private String type;
        private String senderName;
        private String senderAvatarUrl;
        private String startupId;
        private String startupName;
        private String startupLogoUrl;
        private String message;
        private LocalDateTime timestamp;
    }
}
