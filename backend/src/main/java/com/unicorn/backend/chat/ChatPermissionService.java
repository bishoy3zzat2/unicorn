package com.unicorn.backend.chat;

import com.unicorn.backend.investor.InvestorProfile;
import com.unicorn.backend.investor.InvestorProfileRepository;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.subscription.Subscription;
import com.unicorn.backend.subscription.SubscriptionPlan;
import com.unicorn.backend.subscription.SubscriptionRepository;
import com.unicorn.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for validating chat permissions based on user roles and
 * subscriptions.
 * Implements the business rules:
 * - Verified investors: can message any startup (unlimited)
 * - Free/Pro startups: cannot initiate messages to investors
 * - Elite startups: can send one introductory message per investor per calendar
 * month
 */
@Service
@RequiredArgsConstructor
public class ChatPermissionService {

    private final InvestorProfileRepository investorProfileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final MonthlyMessageLimitRepository monthlyMessageLimitRepository;
    private final ChatBlockRepository chatBlockRepository;

    /**
     * Check if a user can initiate a new chat with a startup.
     *
     * @param user    the user attempting to start the chat
     * @param startup the target startup
     * @return true if allowed, false otherwise
     */
    public boolean canInitiateChat(User user, Startup startup) {
        // Check if either party has blocked the other
        if (isBlocked(user, startup.getOwner())) {
            return false;
        }

        // Check if user is a verified investor
        Optional<InvestorProfile> investorProfile = investorProfileRepository.findByUser(user);
        if (investorProfile.isPresent() && investorProfile.get().getIsVerified()) {
            // Verified investors can message any startup unlimited
            return true;
        }

        // If not an investor, check if user is the owner of a startup with Elite
        // subscription
        if (startup.getOwner().getId().equals(user.getId())) {
            // User is trying to message themselves (doesn't make sense)
            return false;
        }

        // For startup owners trying to message investors, check their subscription
        // This would be handled in sendChatRequest, not here
        // Direct initiation is only for investors
        return false;
    }

    /**
     * Check if a startup owner can send a chat request to an investor.
     * Only Elite plan startups can do this, and only once per investor per month.
     *
     * @param startup  the startup
     * @param investor the target investor
     * @return true if allowed, false otherwise
     */
    public boolean canSendChatRequest(Startup startup, User investor) {
        // Check blocking
        if (isBlocked(startup.getOwner(), investor)) {
            return false;
        }

        // Get the startup owner's active subscription
        Optional<Subscription> activeSub = subscriptionRepository.findActiveByUserId(startup.getOwner().getId());

        if (activeSub.isEmpty() || activeSub.get().getPlanType() != SubscriptionPlan.ELITE) {
            // Only Elite plan can send requests
            return false;
        }

        // Check if already messaged this investor this month
        return !hasAlreadyMessagedThisMonth(startup, investor);
    }

    /**
     * Check if a user can send a message in an existing chat.
     *
     * @param user the user attempting to send a message
     * @param chat the chat conversation
     * @return true if allowed, false otherwise
     */
    public boolean canSendMessage(User user, Chat chat) {
        // Check if chat is active
        if (chat.getStatus() != ChatStatus.ACTIVE) {
            return false;
        }

        // Check if user is a participant (either investor or startup owner)
        boolean isInvestor = chat.getInvestor().getId().equals(user.getId());
        boolean isStartupOwner = chat.getStartup().getOwner().getId().equals(user.getId());

        if (!isInvestor && !isStartupOwner) {
            return false;
        }

        // Check blocking
        User otherParty = isInvestor ? chat.getStartup().getOwner() : chat.getInvestor();
        return !isBlocked(user, otherParty);
    }

    /**
     * Check if an Elite startup has already messaged a specific investor this
     * month.
     *
     * @param startup  the startup
     * @param investor the investor
     * @return true if already messaged, false otherwise
     */
    public boolean hasAlreadyMessagedThisMonth(Startup startup, User investor) {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        return monthlyMessageLimitRepository.existsByStartupAndInvestorAndMonthAndYear(
                startup, investor, currentMonth, currentYear);
    }

    /**
     * Check if two users have a blocking relationship.
     *
     * @param user1 first user
     * @param user2 second user
     * @return true if either has blocked the other
     */
    private boolean isBlocked(User user1, User user2) {
        return chatBlockRepository.existsByBlockerAndBlocked(user1, user2) ||
                chatBlockRepository.existsByBlockerAndBlocked(user2, user1);
    }
}
