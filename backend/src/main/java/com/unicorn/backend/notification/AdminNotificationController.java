package com.unicorn.backend.notification;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Admin controller for notification management.
 * Provides endpoints for viewing all notifications, statistics, and sending
 * announcements.
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminNotificationController {

    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    /**
     * Get notification statistics for the dashboard.
     */
    @GetMapping("/stats")
    public ResponseEntity<NotificationStatsDTO> getStats() {
        long total = notificationRepository.count();
        long unread = notificationRepository.countByReadFalse();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = notificationRepository.countCreatedAfter(startOfDay);

        // Find top notification type
        List<Object[]> topTypes = notificationRepository.findTopNotificationTypes();
        String topType = "NONE";
        long topTypeCount = 0;
        if (!topTypes.isEmpty()) {
            Object[] top = topTypes.get(0);
            topType = ((NotificationType) top[0]).name();
            topTypeCount = (Long) top[1];
        }

        NotificationStatsDTO stats = NotificationStatsDTO.of(total, unread, todayCount, topType, topTypeCount);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get all notifications (paginated) with optional filters.
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDTO>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<Notification> notifications;

        if (type != null && read != null) {
            NotificationType notifType = NotificationType.valueOf(type);
            notifications = notificationRepository.findByTypeAndReadOrderByCreatedAtDesc(notifType, read, pageable);
        } else if (type != null) {
            NotificationType notifType = NotificationType.valueOf(type);
            notifications = notificationRepository.findByTypeOrderByCreatedAtDesc(notifType, pageable);
        } else if (read != null) {
            notifications = notificationRepository.findByReadOrderByCreatedAtDesc(read, pageable);
        } else {
            notifications = notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Page<NotificationDTO> dtos = notifications.map(n -> NotificationDTO.from(n, Map.of()));
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get notifications for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<NotificationDTO> notifications = notificationService.getUserNotifications(userId, page, size);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Send a system announcement as a broadcast (1 record, visible to all matching
     * users).
     */
    @PostMapping("/announce")
    public ResponseEntity<Map<String, Object>> sendAnnouncement(@RequestBody SendAnnouncementRequest request) {
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }

        // For SPECIFIC_USER, create individual notification
        if (request.targetAudience() == SendAnnouncementRequest.TargetAudience.SPECIFIC_USER) {
            User user = null;

            // Try to find by ID first, then by email
            if (request.specificUserId() != null) {
                user = userRepository.findById(request.specificUserId()).orElse(null);
            } else if (request.specificUserEmail() != null && !request.specificUserEmail().isBlank()) {
                user = userRepository.findByEmail(request.specificUserEmail()).orElse(null);
            }

            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "User not found"));
            }

            Set<NotificationChannel> channels = request.channels() != null
                    ? request.channels()
                    : Set.of(NotificationChannel.IN_APP);

            notificationService.send(
                    user,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    request.title(),
                    request.message(),
                    Map.of("isAnnouncement", true),
                    null,
                    channels);

            log.info("Announcement sent to specific user: {}", user.getEmail());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Announcement sent to " + user.getEmail(),
                    "sentCount", 1,
                    "isBroadcast", false));
        }

        // For broadcast targets (ALL_USERS, INVESTORS_ONLY, STARTUP_OWNERS_ONLY)
        String targetAudienceStr = switch (request.targetAudience()) {
            case ALL_USERS -> "ALL_USERS";
            case INVESTORS_ONLY -> "INVESTOR";
            case STARTUP_OWNERS_ONLY -> "STARTUP_OWNER";
            default -> "ALL_USERS";
        };

        // Create single broadcast notification
        Notification broadcast = Notification.builder()
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .title(request.title())
                .message(request.message())
                .data("{\"isAnnouncement\":true}")
                .broadcast(true)
                .targetAudience(targetAudienceStr)
                .build();

        notificationRepository.save(broadcast);

        // Count target users for response
        long targetCount = switch (request.targetAudience()) {
            case ALL_USERS -> userRepository.countByStatus("ACTIVE");
            case INVESTORS_ONLY -> userRepository.countByRole("INVESTOR");
            case STARTUP_OWNERS_ONLY -> userRepository.countByRole("STARTUP_OWNER");
            default -> 0;
        };

        log.info("Broadcast announcement created: '{}' for audience: {} (~{} users)",
                request.title(), targetAudienceStr, targetCount);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Broadcast announcement created",
                "sentCount", targetCount,
                "isBroadcast", true));
    }

    /**
     * Delete old read notifications.
     */
    @DeleteMapping("/cleanup")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications(
            @RequestParam(defaultValue = "30") int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        int deletedCount = notificationRepository.deleteOldReadNotifications(cutoffDate);

        log.info("Cleaned up {} old read notifications (older than {} days)", deletedCount, daysOld);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Old notifications cleaned up",
                "deletedCount", deletedCount));
    }

    /**
     * Delete a specific notification.
     */
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            return ResponseEntity.notFound().build();
        }
        notificationRepository.deleteById(notificationId);
        return ResponseEntity.ok(Map.of("success", true, "message", "Notification deleted"));
    }

    /**
     * Get available notification types.
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getNotificationTypes() {
        List<String> types = java.util.Arrays.stream(NotificationType.values())
                .map(Enum::name)
                .toList();
        return ResponseEntity.ok(types);
    }

    /**
     * Search users for autocomplete in announcement dialog.
     */
    @GetMapping("/users/search")
    public ResponseEntity<List<Map<String, Object>>> searchUsersForAnnouncement(
            @RequestParam String q) {
        if (q == null || q.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> users = userRepository.searchUsers(q.trim(), pageable);

        List<Map<String, Object>> results = users.getContent().stream()
                .map(u -> {
                    String name = u.getDisplayName();
                    if (name == null || name.isBlank()) {
                        if (u.getFirstName() != null && u.getLastName() != null) {
                            name = u.getFirstName() + " " + u.getLastName();
                        } else {
                            name = u.getUsername();
                        }
                    }
                    return Map.of(
                            "id", (Object) u.getId().toString(),
                            "email", (Object) u.getEmail(),
                            "name", (Object) (name != null ? name : u.getEmail()),
                            "avatarUrl", (Object) (u.getAvatarUrl() != null ? u.getAvatarUrl() : ""));
                })
                .toList();

        return ResponseEntity.ok(results);
    }
}
