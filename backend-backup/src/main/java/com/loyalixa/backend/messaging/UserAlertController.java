package com.loyalixa.backend.messaging;
import com.loyalixa.backend.messaging.dto.AlertRequest;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/user/alerts")
public class UserAlertController {
    private final UserAlertService alertService;
    public UserAlertController(UserAlertService alertService) {
        this.alertService = alertService;
    }
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<UserAlert>> getMyAlerts(
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "sentAt") Pageable pageable
    ) {
        Page<UserAlert> alerts = alertService.getAlertsByRecipient(user.getId(), pageable);
        return ResponseEntity.ok(alerts);
    }
    @PostMapping("/{alertId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserAlert> markAsRead(
            @PathVariable UUID alertId,
            @AuthenticationPrincipal User user
    ) {
        UserAlert updatedAlert = alertService.markAlertAsRead(alertId, user.getId());
        return ResponseEntity.ok(updatedAlert);
    }
    @PostMapping("/send")
    @PreAuthorize("hasAuthority('alert:send') or hasRole('INSTRUCTOR')")
    public ResponseEntity<UserAlert> sendAlert(
            @Valid @RequestBody AlertRequest request,
            @AuthenticationPrincipal User sender
    ) {
        try {
            UserAlert newAlert = alertService.createAlert(request, sender);
            return new ResponseEntity<>(newAlert, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}