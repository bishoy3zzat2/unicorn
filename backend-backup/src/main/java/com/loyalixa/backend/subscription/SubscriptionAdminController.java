package com.loyalixa.backend.subscription;
import com.loyalixa.backend.subscription.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/subscriptions")
public class SubscriptionAdminController {
    private final SubscriptionService subscriptionService;
    public SubscriptionAdminController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    @PostMapping("/plans")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:create')")
    public ResponseEntity<?> createPlan(@Valid @RequestBody SubscriptionPlanRequest request) {
        try {
            SubscriptionPlanResponse plan = subscriptionService.createPlan(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(plan);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create plan: " + e.getMessage()));
        }
    }
    @PutMapping("/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:update')")
    public ResponseEntity<?> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {
        try {
            SubscriptionPlanResponse plan = subscriptionService.updatePlan(planId, request);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update plan: " + e.getMessage()));
        }
    }
    @GetMapping("/plans")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:view')")
    public ResponseEntity<List<SubscriptionPlanResponse>> getAllPlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getAllPlans();
        return ResponseEntity.ok(plans);
    }
    @GetMapping("/plans/active")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:view')")
    public ResponseEntity<List<SubscriptionPlanResponse>> getActivePlans() {
        List<SubscriptionPlanResponse> plans = subscriptionService.getActivePlans();
        return ResponseEntity.ok(plans);
    }
    @GetMapping("/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:view')")
    public ResponseEntity<?> getPlanById(@PathVariable UUID planId) {
        try {
            SubscriptionPlanResponse plan = subscriptionService.getPlanById(planId);
            return ResponseEntity.ok(plan);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/plans/{planId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('subscription:delete')")
    public ResponseEntity<?> deletePlan(@PathVariable UUID planId) {
        try {
            subscriptionService.deletePlan(planId);
            return ResponseEntity.ok(Map.of("message", "Plan deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUserSubscription(@Valid @RequestBody UserSubscriptionRequest request) {
        try {
            UserSubscriptionResponse subscription = subscriptionService.createUserSubscription(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create subscription: " + e.getMessage()));
        }
    }
    @GetMapping("/users/{userId}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveUserSubscription(@PathVariable UUID userId) {
        try {
            Optional<UserSubscriptionResponse> subscription = subscriptionService.getActiveUserSubscription(userId);
            if (subscription.isPresent()) {
                return ResponseEntity.ok(subscription.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No active subscription found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get subscription: " + e.getMessage()));
        }
    }
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSubscriptionResponse>> getUserSubscriptions(@PathVariable UUID userId) {
        List<UserSubscriptionResponse> subscriptions = subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }
    @PostMapping("/{subscriptionId}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable UUID subscriptionId,
            @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String reason = body != null ? body.get("reason") : "Cancelled by admin";
            UserSubscriptionResponse subscription = subscriptionService.cancelSubscription(subscriptionId, reason);
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to cancel subscription: " + e.getMessage()));
        }
    }
    @PostMapping("/{subscriptionId}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> renewSubscription(@PathVariable UUID subscriptionId) {
        try {
            UserSubscriptionResponse subscription = subscriptionService.renewSubscription(subscriptionId);
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to renew subscription: " + e.getMessage()));
        }
    }
    @PostMapping("/expire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> expireSubscriptions() {
        try {
            subscriptionService.expireSubscriptions();
            return ResponseEntity.ok(Map.of("message", "Subscriptions expired successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to expire subscriptions: " + e.getMessage()));
        }
    }
}
