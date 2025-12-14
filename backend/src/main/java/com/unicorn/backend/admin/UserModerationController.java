package com.unicorn.backend.admin;

import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserModerationLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for user moderation operations.
 */
@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class UserModerationController {

        private final UserModerationService moderationService;

        /**
         * Get detailed user information.
         * 
         * GET /api/v1/admin/users/{id}/details
         */
        @GetMapping("/{id}/details")
        public ResponseEntity<UserDetailResponse> getUserDetails(@PathVariable UUID id) {
                UserDetailResponse details = moderationService.getUserDetails(id);
                return ResponseEntity.ok(details);
        }

        /**
         * Get moderation history for a user.
         * 
         * GET /api/v1/admin/users/{id}/moderation-history
         */
        @GetMapping("/{id}/moderation-history")
        public ResponseEntity<List<ModerationLogResponse>> getModerationHistory(@PathVariable UUID id) {
                List<ModerationLogResponse> history = moderationService.getModerationHistory(id);
                return ResponseEntity.ok(history);
        }

        /**
         * Suspend a user (temporary or permanent).
         * 
         * POST /api/v1/admin/users/{id}/suspend
         */
        @PostMapping("/{id}/suspend")
        public ResponseEntity<Map<String, Object>> suspendUser(
                        @PathVariable UUID id,
                        @RequestBody SuspendUserRequest request,
                        @AuthenticationPrincipal User admin) {

                UserModerationLog log = moderationService.suspendUser(
                                id, admin.getId(), admin.getEmail(), request);

                return ResponseEntity.ok(Map.of(
                                "message", request.isPermanent() ? "User permanently banned" : "User suspended",
                                "logId", log.getId().toString(),
                                "expiresAt", log.getExpiresAt() != null ? log.getExpiresAt().toString() : "never"));
        }

        /**
         * Issue a warning to a user.
         * 
         * POST /api/v1/admin/users/{id}/warn
         */
        @PostMapping("/{id}/warn")
        public ResponseEntity<Map<String, Object>> warnUser(
                        @PathVariable UUID id,
                        @RequestBody WarnUserRequest request,
                        @AuthenticationPrincipal User admin) {

                UserModerationLog log = moderationService.warnUser(
                                id, admin.getId(), admin.getEmail(), request);

                return ResponseEntity.ok(Map.of(
                                "message", "Warning issued to user",
                                "logId", log.getId().toString()));
        }

        /**
         * Remove suspension from a user.
         * 
         * POST /api/v1/admin/users/{id}/unsuspend
         */
        @PostMapping("/{id}/unsuspend")
        public ResponseEntity<Map<String, Object>> unsuspendUser(
                        @PathVariable UUID id,
                        @RequestBody(required = false) Map<String, String> body,
                        @AuthenticationPrincipal User admin) {

                String reason = body != null ? body.get("reason") : "Suspension lifted by admin";

                UserModerationLog log = moderationService.unsuspendUser(
                                id, admin.getId(), admin.getEmail(), reason);

                return ResponseEntity.ok(Map.of(
                                "message", "User suspension removed",
                                "logId", log.getId().toString()));
        }

        /**
         * Delete a user (soft or hard delete).
         * 
         * DELETE /api/v1/admin/users/{id}
         * 
         * @param hardDelete if true, permanently removes from database; if false, just
         *                   marks as DELETED
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<Map<String, Object>> deleteUser(
                        @PathVariable UUID id,
                        @RequestParam(required = false, defaultValue = "false") boolean hardDelete,
                        @RequestBody(required = false) DeleteUserRequest request,
                        @AuthenticationPrincipal User admin) {

                String reason = request != null && request.getReason() != null
                                ? request.getReason()
                                : "Deleted by admin";

                if (hardDelete) {
                        // Permanently remove from database
                        moderationService.hardDeleteUser(id);
                        return ResponseEntity.ok(Map.of(
                                        "message", "User permanently deleted from database",
                                        "type", "hard"));
                } else {
                        // Soft delete - mark as DELETED but keep in database
                        UserModerationLog log = moderationService.softDeleteUser(
                                        id, admin.getId(), admin.getEmail(), reason);
                        return ResponseEntity.ok(Map.of(
                                        "message", "User marked as deleted",
                                        "type", "soft",
                                        "logId", log.getId().toString()));
                }
        }
}
