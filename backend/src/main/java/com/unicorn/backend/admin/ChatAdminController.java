package com.unicorn.backend.admin;

import com.unicorn.backend.chat.*;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Admin controller for chat management and moderation.
 * Provides endpoints for viewing chats and moderating reports.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ChatAdminController {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatReportRepository chatReportRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;

    /**
     * Get all chats for a specific user (for admin dashboard).
     * GET /api/admin/users/{userId}/chats
     */
    @GetMapping("/users/{userId}/chats")
    public ResponseEntity<List<ChatResponse>> getUserChats(@PathVariable UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<Chat> chats = chatRepository.findAllChatsForUser(user);
        List<ChatResponse> responses = chats.stream()
                .map(chat -> ChatResponse.fromEntity(chat, 0)) // Unread count not needed for admin view
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all chats for a specific startup (for admin dashboard).
     * GET /api/admin/startups/{startupId}/chats
     */
    @GetMapping("/startups/{startupId}/chats")
    public ResponseEntity<List<ChatResponse>> getStartupChats(@PathVariable UUID startupId) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        List<Chat> chats = chatRepository.findByStartupOrderByLastMessageAtDesc(startup);

        List<ChatResponse> responses = chats.stream()
                .map(chat -> ChatResponse.fromEntity(chat, 0))
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all messages for a specific chat (for admin viewing).
     * GET /api/admin/chats/{chatId}/messages
     */
    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<List<MessageResponse>> getChatMessages(@PathVariable UUID chatId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Get all messages including deleted ones for admin viewing
        List<ChatMessage> messages = chatMessageRepository.findByChatOrderByCreatedAtAsc(chat);
        List<MessageResponse> responses = messages.stream()
                .map(MessageResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get all chat reports (paginated).
     * GET /api/admin/chat-reports?page=0&size=20&status=PENDING
     */
    @GetMapping("/chat-reports")
    public ResponseEntity<Page<ChatReportResponse>> getChatReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReportStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatReport> reports;

        if (status != null) {
            reports = chatReportRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            reports = chatReportRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        Page<ChatReportResponse> responses = reports.map(ChatReportResponse::fromEntity);
        return ResponseEntity.ok(responses);
    }

    /**
     * Review a chat report (mark as reviewed).
     * POST /api/admin/chat-reports/{id}/review
     */
    @PostMapping("/chat-reports/{id}/review")
    public ResponseEntity<ChatReportResponse> reviewReport(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        ChatReport report = chatReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(ReportStatus.REVIEWED);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(admin);

        ChatReport savedReport = chatReportRepository.save(report);
        ChatReportResponse response = ChatReportResponse.fromEntity(savedReport);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete a reported message (soft delete).
     * DELETE /api/admin/chat-messages/{id}
     */
    @DeleteMapping("/chat-messages/{id}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        ChatMessage message = chatMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        message.setIsDeleted(true);
        chatMessageRepository.save(message);

        // Update any reports for this message
        List<ChatReport> reports = chatReportRepository.findByMessage(message);
        reports.forEach(report -> {
            if (report.getStatus() == ReportStatus.PENDING || report.getStatus() == ReportStatus.REVIEWED) {
                report.setStatus(ReportStatus.ACTION_TAKEN);
                report.setReviewedAt(LocalDateTime.now());
                report.setReviewedBy(admin);
            }
        });
        chatReportRepository.saveAll(reports);

        return ResponseEntity.ok().build();
    }

    /**
     * Dismiss a chat report.
     * POST /api/admin/chat-reports/{id}/dismiss
     */
    @PostMapping("/chat-reports/{id}/dismiss")
    public ResponseEntity<ChatReportResponse> dismissReport(
            @PathVariable UUID id,
            @AuthenticationPrincipal User admin) {
        ChatReport report = chatReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setStatus(ReportStatus.DISMISSED);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(admin);

        ChatReport savedReport = chatReportRepository.save(report);
        ChatReportResponse response = ChatReportResponse.fromEntity(savedReport);

        return ResponseEntity.ok(response);
    }
}
