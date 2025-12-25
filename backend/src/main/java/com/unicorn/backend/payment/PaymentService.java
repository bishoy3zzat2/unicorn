package com.unicorn.backend.payment;

import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.model.SubscriptionPurchase;
import com.unicorn.backend.config.GooglePlayConfig;
import com.unicorn.backend.subscription.*;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for managing payment transactions and Google Play subscription
 * verification.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;
    private final AndroidPublisher androidPublisher;
    private final GooglePlayConfig googlePlayConfig;

    // Subscription product ID mappings to plan types
    private static final Map<String, SubscriptionPlan> SUBSCRIPTION_PRODUCT_MAP = Map.of(
            "pro_monthly", SubscriptionPlan.PRO,
            "pro_yearly", SubscriptionPlan.PRO,
            "elite_monthly", SubscriptionPlan.ELITE,
            "elite_yearly", SubscriptionPlan.ELITE);

    // Price mappings for subscription products (in EGP)
    private static final Map<String, BigDecimal> SUBSCRIPTION_PRICE_MAP = Map.of(
            "pro_monthly", new BigDecimal("99.00"),
            "pro_yearly", new BigDecimal("999.00"),
            "elite_monthly", new BigDecimal("199.00"),
            "elite_yearly", new BigDecimal("1999.00"));

    /**
     * Verifies a Google Play purchase and processes the subscription activation.
     * 
     * @param request The purchase verification request
     * @return Response indicating success/failure with subscription details
     */
    @Transactional
    public GooglePlayPurchaseResponse verifyAndProcessGooglePay(GooglePlayPurchaseRequest request) {
        try {
            // Validate the subscription product ID
            SubscriptionPlan plan = SUBSCRIPTION_PRODUCT_MAP.get(request.getSubscriptionId());
            if (plan == null) {
                return GooglePlayPurchaseResponse
                        .failure("Unknown subscription product: " + request.getSubscriptionId());
            }

            // Fetch the user
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + request.getUserId()));

            // Call Google Play API to verify the purchase
            SubscriptionPurchase purchase = verifyWithGooglePlay(
                    request.getSubscriptionId(),
                    request.getPurchaseToken());

            if (purchase == null) {
                return GooglePlayPurchaseResponse.failure("Failed to verify purchase with Google Play");
            }

            // Validate purchase state (0 = Purchased, 1 = Canceled, 2 = Pending)
            Integer paymentState = purchase.getPaymentState();
            if (paymentState == null || paymentState != 1) {
                // PaymentState: 1 = Payment received
                log.warn("Invalid payment state for purchase: {}", paymentState);
                return GooglePlayPurchaseResponse.failure("Payment not completed. State: " + paymentState);
            }

            // Check if subscription is cancelled
            Integer cancelReason = purchase.getCancelReason();
            if (cancelReason != null) {
                log.warn("Subscription has been cancelled. Reason: {}", cancelReason);
                return GooglePlayPurchaseResponse.failure("Subscription has been cancelled");
            }

            // Calculate expiry date from Google's response
            Long expiryTimeMillis = purchase.getExpiryTimeMillis();
            LocalDateTime expiryDate = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(expiryTimeMillis),
                    ZoneId.systemDefault());

            // Check if subscription has already expired
            if (expiryDate.isBefore(LocalDateTime.now())) {
                return GooglePlayPurchaseResponse.failure("Subscription has already expired");
            }

            // Get the price for this subscription
            BigDecimal amount = SUBSCRIPTION_PRICE_MAP.getOrDefault(
                    request.getSubscriptionId(),
                    BigDecimal.ZERO);

            // Create or update the subscription
            createOrUpdateSubscription(
                    user,
                    plan,
                    expiryDate,
                    amount,
                    request.getPurchaseToken());

            // Create a payment record
            Payment payment = createPaymentRecord(
                    user,
                    amount,
                    "Google Play Subscription: " + request.getSubscriptionId(),
                    "GOOGLE_PLAY",
                    purchase.getOrderId());

            log.info("Successfully processed Google Play subscription for user: {}, plan: {}, expires: {}",
                    user.getId(), plan, expiryDate);

            return GooglePlayPurchaseResponse.success(plan, expiryDate, payment.getTransactionId());

        } catch (IOException e) {
            log.error("IO error verifying Google Play purchase", e);
            return GooglePlayPurchaseResponse.failure("Error communicating with Google Play: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing Google Play purchase", e);
            return GooglePlayPurchaseResponse.failure("Error processing purchase: " + e.getMessage());
        }
    }

    /**
     * Verifies a subscription purchase with Google Play API.
     * 
     * @param subscriptionId The subscription product ID
     * @param purchaseToken  The purchase token from Google Play
     * @return The subscription purchase details from Google, or null if
     *         verification failed
     */
    public SubscriptionPurchase verifyWithGooglePlay(String subscriptionId, String purchaseToken) throws IOException {
        try {
            String packageName = googlePlayConfig.getPackageName();

            log.debug("Verifying subscription with Google Play - package: {}, subscription: {}",
                    packageName, subscriptionId);

            SubscriptionPurchase purchase = androidPublisher
                    .purchases()
                    .subscriptions()
                    .get(packageName, subscriptionId, purchaseToken)
                    .execute();

            log.debug("Google Play verification successful. Order ID: {}, Expiry: {}",
                    purchase.getOrderId(), purchase.getExpiryTimeMillis());

            return purchase;

        } catch (IOException e) {
            log.error("Failed to verify subscription with Google Play: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Creates or updates a subscription for the user.
     */
    private Subscription createOrUpdateSubscription(
            User user,
            SubscriptionPlan plan,
            LocalDateTime expiryDate,
            BigDecimal amount,
            String purchaseToken) {

        // Cancel any existing active subscription
        subscriptionRepository.findActiveByUserId(user.getId()).ifPresent(existing -> {
            existing.setStatus(SubscriptionStatus.CANCELLED);
            existing.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(existing);
        });

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .planType(plan)
                .startDate(LocalDateTime.now())
                .endDate(expiryDate)
                .status(SubscriptionStatus.ACTIVE)
                .amount(amount)
                .currency("EGP")
                .googlePlayPurchaseToken(purchaseToken)
                .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Creates a payment record for the transaction.
     */
    private Payment createPaymentRecord(
            User user,
            BigDecimal amount,
            String description,
            String paymentMethod,
            String orderId) {

        String transactionId = orderId != null ? orderId : generateTransactionId();

        Payment payment = Payment.builder()
                .transactionId(transactionId)
                .user(user)
                .amount(amount)
                .currency("EGP")
                .status(PaymentStatus.COMPLETED)
                .description(description)
                .paymentMethod(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * Create a new payment record.
     */
    @Transactional
    public Payment createPayment(UUID userId, BigDecimal amount, String description, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Payment payment = Payment.builder()
                .transactionId(generateTransactionId())
                .user(user)
                .amount(amount)
                .currency("EGP")
                .status(PaymentStatus.PENDING)
                .description(description)
                .paymentMethod(paymentMethod)
                .timestamp(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /**
     * Mark payment as completed.
     */
    @Transactional
    public Payment completePayment(String transactionId) {
        Payment payment = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        payment.setStatus(PaymentStatus.COMPLETED);
        return paymentRepository.save(payment);
    }

    /**
     * Mark payment as failed.
     */
    @Transactional
    public Payment failPayment(String transactionId) {
        Payment payment = paymentRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + transactionId));
        payment.setStatus(PaymentStatus.FAILED);
        return paymentRepository.save(payment);
    }

    /**
     * Get recent payments.
     */
    public Page<Payment> getRecentPayments(int limit) {
        return paymentRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }

    /**
     * Get payments for a user.
     */
    public Page<Payment> getPaymentsForUser(UUID userId, Pageable pageable) {
        return paymentRepository.findByUserIdOrderByTimestampDesc(userId, pageable);
    }

    /**
     * Get monthly revenue data for charts.
     */
    public List<Map<String, Object>> getMonthlyRevenueData() {
        LocalDateTime startOfYear = LocalDateTime.now().withMonth(1).withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);

        List<Object[]> rawData = paymentRepository.getMonthlyRevenue(startOfYear);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rawData) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", row[0]);
            monthData.put("revenue", row[2]);
            result.add(monthData);
        }

        return result;
    }

    /**
     * Get total revenue for current month.
     */
    public BigDecimal getCurrentMonthRevenue() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        BigDecimal revenue = paymentRepository.getTotalRevenueForPeriod(startOfMonth, endOfMonth);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    /**
     * Generate unique transaction ID.
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
