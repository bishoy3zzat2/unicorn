package com.unicorn.backend.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for payment-related operations.
 * Handles Google Play subscription verification and payment management.
 */
@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Verifies a Google Play subscription purchase and activates the subscription.
     * 
     * This endpoint should be called by the mobile app after a successful
     * Google Play purchase to verify the transaction and activate the subscription.
     * 
     * @param request The purchase verification request containing token and
     *                subscription ID
     * @return Response indicating success/failure with subscription details
     */
    @PostMapping("/google-play/verify")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<GooglePlayPurchaseResponse> verifyGooglePlayPurchase(
            @Valid @RequestBody GooglePlayPurchaseRequest request) {

        log.info("Received Google Play purchase verification request for user: {}, subscription: {}",
                request.getUserId(), request.getSubscriptionId());

        try {
            GooglePlayPurchaseResponse response = paymentService.verifyAndProcessGooglePay(request);

            if (response.isSuccess()) {
                log.info("Successfully verified and activated subscription for user: {}", request.getUserId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Google Play verification failed for user: {} - {}",
                        request.getUserId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error processing Google Play purchase for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError()
                    .body(GooglePlayPurchaseResponse.failure("Internal error processing purchase: " + e.getMessage()));
        }
    }

    /**
     * Verifies a Google Play one-time purchase for investor verification fee
     * and marks the investor as verified.
     * 
     * This endpoint should be called by the mobile app after a successful
     * Google Play purchase to verify the transaction and grant the verified badge.
     * 
     * @param request The verification purchase request containing token and product
     *                ID
     * @return Response indicating success/failure with verification details
     */
    @PostMapping("/google-play/verify-verification")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VerificationPurchaseResponse> verifyInvestorVerificationPurchase(
            @Valid @RequestBody VerificationPurchaseRequest request) {

        log.info("Received investor verification purchase request for user: {}, product: {}",
                request.getUserId(), request.getProductId());

        try {
            VerificationPurchaseResponse response = paymentService.verifyAndProcessVerificationPayment(request);

            if (response.isSuccess()) {
                log.info("Successfully verified investor for user: {}", request.getUserId());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Investor verification failed for user: {} - {}",
                        request.getUserId(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error processing investor verification for user: {}", request.getUserId(), e);
            return ResponseEntity.internalServerError()
                    .body(VerificationPurchaseResponse
                            .failure("Internal error processing verification: " + e.getMessage()));
        }
    }

    /**
     * Gets the payment history for the current user.
     * Can be filtered by status and paginated.
     * 
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return Paginated list of payments
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getPaymentHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Implementation to be added when needed
        return ResponseEntity.ok().build();
    }
}
