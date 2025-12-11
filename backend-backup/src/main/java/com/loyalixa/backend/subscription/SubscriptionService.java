package com.loyalixa.backend.subscription;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loyalixa.backend.course.EnrollmentRepository;
import com.loyalixa.backend.subscription.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.lxcoins.LXCoinsAccount;
import com.loyalixa.backend.lxcoins.LXCoinsAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ObjectMapper objectMapper;
    private final LXCoinsAccountRepository lxCoinsAccountRepository;

    public SubscriptionService(SubscriptionPlanRepository planRepository,
            UserSubscriptionRepository subscriptionRepository,
            UserRepository userRepository,
            EnrollmentRepository enrollmentRepository,
            ObjectMapper objectMapper,
            LXCoinsAccountRepository lxCoinsAccountRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.objectMapper = objectMapper;
        this.lxCoinsAccountRepository = lxCoinsAccountRepository;
    }

    // ========== Subscription Plan Methods ==========

    @Transactional
    public SubscriptionPlanResponse createPlan(SubscriptionPlanRequest request) {
        // Check if code already exists (code is now required)
        if (planRepository.existsByCode(request.code())) {
            throw new IllegalStateException("Plan with code '" + request.code() + "' already exists");
        }

        // Check if name already exists
        if (planRepository.existsByName(request.name())) {
            throw new IllegalStateException("Plan with name '" + request.name() + "' already exists");
        }

        // Check if setting as default and another default plan exists
        if (request.isDefault() != null && request.isDefault()) {
            Optional<SubscriptionPlan> existingDefault = planRepository.findByIsDefaultTrue();
            if (existingDefault.isPresent()) {
                // Unset the existing default plan
                SubscriptionPlan oldDefault = existingDefault.get();
                oldDefault.setIsDefault(false);
                planRepository.save(oldDefault);
            }
        }

        SubscriptionPlan plan = new SubscriptionPlan();
        mapRequestToPlan(request, plan);

        SubscriptionPlan saved = planRepository.save(plan);
        return mapPlanToResponse(saved);
    }

    @Transactional
    public SubscriptionPlanResponse updatePlan(UUID planId, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        // Check code uniqueness if changed (code is now required)
        if (!request.code().equals(plan.getCode())) {
            if (planRepository.existsByCode(request.code())) {
                throw new IllegalStateException("Plan with code '" + request.code() + "' already exists");
            }
        }

        // Check name uniqueness if changed
        if (!request.name().equals(plan.getName())) {
            if (planRepository.existsByName(request.name())) {
                throw new IllegalStateException("Plan with name '" + request.name() + "' already exists");
            }
        }

        // Check if setting as default and another default plan exists
        if (request.isDefault() != null && request.isDefault() && !plan.getIsDefault()) {
            Optional<SubscriptionPlan> existingDefault = planRepository.findByIsDefaultTrue();
            if (existingDefault.isPresent() && !existingDefault.get().getId().equals(planId)) {
                // Unset the existing default plan
                SubscriptionPlan oldDefault = existingDefault.get();
                oldDefault.setIsDefault(false);
                planRepository.save(oldDefault);
            }
        }

        // Check if maxDevices is being changed
        Integer oldMaxDevices = plan.getMaxDevices();
        Integer newMaxDevices = request.maxDevices();
        boolean maxDevicesChanged = (oldMaxDevices == null && newMaxDevices != null) ||
                (oldMaxDevices != null && !oldMaxDevices.equals(newMaxDevices));

        mapRequestToPlan(request, plan);
        SubscriptionPlan saved = planRepository.save(plan);

        // If maxDevices changed, update all users with active subscriptions to this
        // plan
        if (maxDevicesChanged && newMaxDevices != null && newMaxDevices > 0) {
            updateMaxDevicesForActiveSubscribers(planId, newMaxDevices);
        }

        return mapPlanToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapPlanToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getActivePlans() {
        return planRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::mapPlanToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getPlanById(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));
        return mapPlanToResponse(plan);
    }

    @Transactional
    public void deletePlan(UUID planId) {
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        // Check if there are active subscriptions
        Long activeCount = subscriptionRepository.countActiveSubscriptionsByPlanId(planId, LocalDateTime.now());
        if (activeCount > 0) {
            throw new IllegalStateException("Cannot delete plan with active subscriptions. Deactivate it instead.");
        }

        planRepository.delete(plan);
    }

    // ========== User Subscription Methods ==========

    @Transactional
    public UserSubscriptionResponse createUserSubscription(UserSubscriptionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SubscriptionPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        if (!plan.getIsActive()) {
            throw new IllegalStateException("Cannot subscribe to inactive plan");
        }

        // Cancel or expire existing active subscription
        Optional<UserSubscription> existing = subscriptionRepository.findActiveSubscriptionByUserId(user.getId(),
                LocalDateTime.now());
        if (existing.isPresent()) {
            UserSubscription oldSub = existing.get();
            oldSub.setStatus("CANCELLED");
            oldSub.setCancelledAt(LocalDateTime.now());
            oldSub.setCancellationReason("Replaced by new subscription");
            subscriptionRepository.save(oldSub);
        }

        // Create new subscription
        UserSubscription subscription = new UserSubscription();
        subscription.setUser(user);
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");
        subscription.setStartDate(request.startDate() != null ? request.startDate() : LocalDateTime.now());

        // Calculate end date
        if (plan.getDurationDays() != null && plan.getDurationDays() > 0) {
            subscription.setEndDate(subscription.getStartDate().plusDays(plan.getDurationDays()));
        } else {
            subscription.setEndDate(null); // Free plan or lifetime
        }

        subscription.setAutoRenew(request.autoRenew() != null ? request.autoRenew() : false);
        subscription.setPaymentReference(request.paymentReference());
        subscription.setNotes(request.notes());

        // Update user's max devices
        user.setMaxDevices(plan.getMaxDevices());
        userRepository.save(user);

        // Activate LXCoins account if subscribing to a non-free plan
        if (!"FREE".equalsIgnoreCase(plan.getCode())) {
            activateLXCoinsAccount(user.getId());
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        return mapSubscriptionToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UserSubscriptionResponse> getActiveUserSubscription(UUID userId) {
        Optional<UserSubscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId,
                LocalDateTime.now());
        return subscription.map(this::mapSubscriptionToResponse);
    }

    @Transactional(readOnly = true)
    public List<UserSubscriptionResponse> getUserSubscriptions(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByStartDateDesc(userId).stream()
                .map(this::mapSubscriptionToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserSubscriptionResponse cancelSubscription(UUID subscriptionId, String reason) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        subscription.setStatus("CANCELLED");
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);

        UserSubscription saved = subscriptionRepository.save(subscription);
        return mapSubscriptionToResponse(saved);
    }

    @Transactional
    public UserSubscriptionResponse renewSubscription(UUID subscriptionId) {
        UserSubscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        if (!"ACTIVE".equals(subscription.getStatus())) {
            throw new IllegalStateException("Can only renew active subscriptions");
        }

        SubscriptionPlan plan = subscription.getPlan();
        if (plan.getDurationDays() == null || plan.getDurationDays() <= 0) {
            throw new IllegalStateException("Cannot renew subscription with no duration");
        }

        LocalDateTime newEndDate = subscription.getEndDate() != null
                ? subscription.getEndDate().plusDays(plan.getDurationDays())
                : LocalDateTime.now().plusDays(plan.getDurationDays());

        subscription.setEndDate(newEndDate);
        subscription.setLastRenewedAt(LocalDateTime.now());
        subscription.setRenewalCount(subscription.getRenewalCount() + 1);

        // Update user's max devices from plan
        User user = subscription.getUser();
        if (plan.getMaxDevices() != null && plan.getMaxDevices() > 0) {
            user.setMaxDevices(plan.getMaxDevices());
            userRepository.save(user);
        }

        UserSubscription saved = subscriptionRepository.save(subscription);
        return mapSubscriptionToResponse(saved);
    }

    @Transactional
    public void expireSubscriptions() {
        subscriptionRepository.expireSubscriptions(LocalDateTime.now());
    }

    // ========== Helper Methods ==========

    private void mapRequestToPlan(SubscriptionPlanRequest request, SubscriptionPlan plan) {
        plan.setName(request.name());
        plan.setCode(request.code());
        plan.setDescription(request.description());
        plan.setPlanType(request.planType());
        plan.setDurationDays(request.durationDays());
        plan.setPrice(request.price());
        plan.setCurrency(request.currency() != null ? request.currency() : "USD");
        plan.setIsActive(request.isActive() != null ? request.isActive() : true);
        plan.setIsDefault(request.isDefault() != null ? request.isDefault() : false);
        plan.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);

        // Course features
        if (request.allowedCourseTypes() != null) {
            try {
                plan.setAllowedCourseTypes(objectMapper.writeValueAsString(request.allowedCourseTypes()));
            } catch (Exception e) {
                throw new IllegalStateException("Invalid allowedCourseTypes format", e);
            }
        }
        plan.setMaxCourseEnrollments(request.maxCourseEnrollments());
        plan.setMaxBundleEnrollments(request.maxBundleEnrollments());

        // LXCoins features
        plan.setDailyCoins(request.dailyCoins());
        if (request.streakBonusCoins() != null) {
            try {
                plan.setStreakBonusCoins(objectMapper.writeValueAsString(request.streakBonusCoins()));
            } catch (Exception e) {
                throw new IllegalStateException("Invalid streakBonusCoins format", e);
            }
        }
        plan.setCourseCompletionCoins(request.courseCompletionCoins());
        plan.setCourseEnrollmentCoins(request.courseEnrollmentCoins());
        plan.setCourseReviewCoins(request.courseReviewCoins());
        plan.setCertificateCoins(request.certificateCoins());
        plan.setInvitationCoins(request.invitationCoins());

        // Device features
        plan.setMaxDevices(request.maxDevices());

        // Additional features
        plan.setHasPremiumAccess(request.hasPremiumAccess() != null ? request.hasPremiumAccess() : false);
        plan.setHasDownloadAccess(request.hasDownloadAccess() != null ? request.hasDownloadAccess() : false);
        plan.setHasCertificateAccess(request.hasCertificateAccess() != null ? request.hasCertificateAccess() : false);
        plan.setHasPremiumSupport(request.hasPremiumSupport() != null ? request.hasPremiumSupport() : false);
        plan.setHasExclusiveContent(request.hasExclusiveContent() != null ? request.hasExclusiveContent() : false);
        plan.setHasLiveCourses(request.hasLiveCourses() != null ? request.hasLiveCourses() : false);
        plan.setHasQuizzesAccess(request.hasQuizzesAccess() != null ? request.hasQuizzesAccess() : true);
        plan.setHasCommunityAccess(request.hasCommunityAccess() != null ? request.hasCommunityAccess() : true);

        // Additional features JSON
        if (request.additionalFeatures() != null) {
            try {
                plan.setAdditionalFeatures(objectMapper.writeValueAsString(request.additionalFeatures()));
            } catch (Exception e) {
                throw new IllegalStateException("Invalid additionalFeatures format", e);
            }
        }
    }

    private SubscriptionPlanResponse mapPlanToResponse(SubscriptionPlan plan) {
        List<String> allowedCourseTypes = null;
        if (plan.getAllowedCourseTypes() != null) {
            try {
                allowedCourseTypes = objectMapper.readValue(plan.getAllowedCourseTypes(),
                        new TypeReference<List<String>>() {
                        });
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        Map<String, BigDecimal> streakBonusCoins = null;
        if (plan.getStreakBonusCoins() != null) {
            try {
                streakBonusCoins = objectMapper.readValue(plan.getStreakBonusCoins(),
                        new TypeReference<Map<String, BigDecimal>>() {
                        });
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        Map<String, Object> additionalFeatures = null;
        if (plan.getAdditionalFeatures() != null) {
            try {
                additionalFeatures = objectMapper.readValue(plan.getAdditionalFeatures(),
                        new TypeReference<Map<String, Object>>() {
                        });
            } catch (Exception e) {
                // Ignore parsing errors
            }
        }

        Long activeCount = subscriptionRepository.countActiveSubscriptionsByPlanId(plan.getId(), LocalDateTime.now());

        return new SubscriptionPlanResponse(
                plan.getId(), plan.getName(), plan.getCode(), plan.getDescription(),
                plan.getPlanType(), plan.getDurationDays(), plan.getPrice(), plan.getCurrency(),
                plan.getIsActive(), plan.getIsDefault(), plan.getDisplayOrder(),
                allowedCourseTypes, plan.getMaxCourseEnrollments(), plan.getMaxBundleEnrollments(),
                plan.getDailyCoins(), streakBonusCoins,
                plan.getCourseCompletionCoins(), plan.getCourseEnrollmentCoins(),
                plan.getCourseReviewCoins(), plan.getCertificateCoins(), plan.getInvitationCoins(),
                plan.getMaxDevices(),
                plan.getHasPremiumAccess(), plan.getHasDownloadAccess(), plan.getHasCertificateAccess(),
                plan.getHasPremiumSupport(), plan.getHasExclusiveContent(), plan.getHasLiveCourses(),
                plan.getHasQuizzesAccess(), plan.getHasCommunityAccess(),
                additionalFeatures,
                plan.getCreatedAt(), plan.getUpdatedAt(),
                activeCount);
    }

    private UserSubscriptionResponse mapSubscriptionToResponse(UserSubscription subscription) {
        User user = subscription.getUser();
        SubscriptionPlan plan = subscription.getPlan();

        return new UserSubscriptionResponse(
                subscription.getId(),
                user.getId(), user.getEmail(), user.getUsername(),
                plan.getId(), plan.getName(), plan.getCode(),
                subscription.getStatus(),
                subscription.getStartDate(), subscription.getEndDate(),
                subscription.getCancelledAt(), subscription.getCancellationReason(),
                subscription.getAutoRenew(), subscription.getLastRenewedAt(),
                subscription.getRenewalCount(),
                subscription.getPaymentReference(), subscription.getNotes(),
                subscription.getCreatedAt(), subscription.getUpdatedAt(),
                subscription.isActive(), subscription.isExpired());
    }

    // ========== Utility Methods for Other Services ==========

    /**
     * Get active subscription for user (used by other services)
     * If user has no active subscription, returns the default plan if available
     */
    @Transactional(readOnly = true)
    public Optional<SubscriptionPlan> getActivePlanForUser(UUID userId) {
        Optional<UserSubscription> subscription = subscriptionRepository.findActiveSubscriptionByUserId(userId,
                LocalDateTime.now());
        if (subscription.isPresent()) {
            return subscription.map(UserSubscription::getPlan);
        }

        // If no active subscription, return default plan
        Optional<SubscriptionPlan> defaultPlan = planRepository.findByIsDefaultTrue();
        return defaultPlan;
    }

    /**
     * Check if user can enroll in more courses
     */
    @Transactional(readOnly = true)
    public boolean canEnrollInCourse(UUID userId, boolean isBundle) {
        Optional<SubscriptionPlan> planOpt = getActivePlanForUser(userId);
        if (planOpt.isEmpty()) {
            return false; // No active subscription
        }

        SubscriptionPlan plan = planOpt.get();
        if (isBundle) {
            if (plan.getMaxBundleEnrollments() == null) {
                return true; // Unlimited
            }
            // Count current bundle enrollments (courses that are part of bundles)
            long bundleEnrollments = enrollmentRepository.findByStudentIdWithCourse(userId).stream()
                    .filter(e -> "ACTIVE".equals(e.getEnrollmentStatus()))
                    .filter(e -> e.getCourse() != null && e.getCourse().getBundles() != null
                            && !e.getCourse().getBundles().isEmpty())
                    .count();
            return bundleEnrollments < plan.getMaxBundleEnrollments();
        } else {
            if (plan.getMaxCourseEnrollments() == null) {
                return true; // Unlimited
            }
            // Count current course enrollments (excluding bundle enrollments)
            long courseEnrollments = enrollmentRepository.findByStudentIdWithCourse(userId).stream()
                    .filter(e -> "ACTIVE".equals(e.getEnrollmentStatus()))
                    .filter(e -> e.getCourse() != null
                            && (e.getCourse().getBundles() == null || e.getCourse().getBundles().isEmpty()))
                    .count();
            return courseEnrollments < plan.getMaxCourseEnrollments();
        }
    }

    /**
     * Get LXCoins reward amount for activity based on subscription
     */
    @Transactional(readOnly = true)
    public BigDecimal getCoinsRewardForActivity(UUID userId, String activityType) {
        Optional<SubscriptionPlan> planOpt = getActivePlanForUser(userId);
        if (planOpt.isEmpty()) {
            return BigDecimal.ZERO; // No active subscription
        }

        SubscriptionPlan plan = planOpt.get();
        switch (activityType) {
            case "DAILY_LOGIN":
                return plan.getDailyCoins();
            case "COURSE_COMPLETION":
                return plan.getCourseCompletionCoins();
            case "COURSE_ENROLLMENT":
                return plan.getCourseEnrollmentCoins();
            case "COURSE_REVIEW":
                return plan.getCourseReviewCoins();
            case "CERTIFICATE":
                return plan.getCertificateCoins();
            case "INVITATION":
                return plan.getInvitationCoins();
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Update maxDevices for all users with active subscriptions to a specific plan
     */
    @Transactional
    private void updateMaxDevicesForActiveSubscribers(UUID planId, Integer newMaxDevices) {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> activeSubscriptions = subscriptionRepository.findActiveSubscriptionsByPlanId(planId,
                now);

        int updatedCount = 0;
        for (UserSubscription subscription : activeSubscriptions) {
            User user = subscription.getUser();
            if (user != null) {
                user.setMaxDevices(newMaxDevices);
                userRepository.save(user);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            System.out.println("[SubscriptionService] Updated maxDevices to " + newMaxDevices +
                    " for " + updatedCount + " users with active subscriptions to plan: " + planId);
        }
    }

    /**
     * Activate LXCoins account for user
     * Called when user subscribes to any non-free plan
     */
    @Transactional
    private void activateLXCoinsAccount(UUID userId) {
        Optional<LXCoinsAccount> accountOpt = lxCoinsAccountRepository.findByUserId(userId);
        if (accountOpt.isPresent()) {
            LXCoinsAccount account = accountOpt.get();
            if (account.getIsActive() == null || !account.getIsActive()) {
                account.setIsActive(true);
                lxCoinsAccountRepository.save(account);
            }
        } else {
            // If account doesn't exist, create it as active
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            LXCoinsAccount account = new LXCoinsAccount();
            account.setUser(user);
            account.setBalance(BigDecimal.ZERO);
            account.setTotalEarned(BigDecimal.ZERO);
            account.setTotalSpent(BigDecimal.ZERO);
            account.setIsActive(true);
            lxCoinsAccountRepository.save(account);
        }
    }
}
