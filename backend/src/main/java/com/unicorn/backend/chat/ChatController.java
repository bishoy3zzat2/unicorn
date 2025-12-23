package com.unicorn.backend.chat;

import com.unicorn.backend.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for chat operations.
 * Handles chat initiation, messaging, requests, blocking, and reporting.
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Start a new chat (Investor → Startup).
     * POST /api/chats/start
     */
    @PostMapping("/start")
    public ResponseEntity<ChatResponse> startChat(
            @Valid @RequestBody StartChatRequest request,
            @AuthenticationPrincipal User user) {
        Chat chat = chatService.startChat(user, request.startupId());
        long unreadCount = chatMessageRepository.countUnreadMessages(chat, user);
        ChatResponse response = ChatResponse.fromEntity(chat, unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Send a chat request (Elite Startup → Investor).
     * POST /api/chats/request
     */
    @PostMapping("/request")
    public ResponseEntity<ChatRequestResponse> sendChatRequest(
            @Valid @RequestBody SendChatRequestRequest request,
            @AuthenticationPrincipal User user) {
        ChatRequest chatRequest = chatService.sendChatRequest(
                request.startupId(),
                request.investorId(),
                request.initialMessage(),
                user);
        ChatRequestResponse response = ChatRequestResponse.fromEntity(chatRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Accept a chat request.
     * POST /api/chats/requests/{id}/accept
     */
    @PostMapping("/requests/{id}/accept")
    public ResponseEntity<ChatResponse> acceptChatRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        Chat chat = chatService.acceptChatRequest(id, user);
        long unreadCount = chatMessageRepository.countUnreadMessages(chat, user);
        ChatResponse response = ChatResponse.fromEntity(chat, unreadCount);
        return ResponseEntity.ok(response);
    }

    /**
     * Decline a chat request.
     * POST /api/chats/requests/{id}/decline
     */
    @PostMapping("/requests/{id}/decline")
    public ResponseEntity<Void> declineChatRequest(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        chatService.declineChatRequest(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Get pending chat requests for the authenticated user (investor).
     * GET /api/chats/requests/pending
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<ChatRequestResponse>> getPendingRequests(
            @AuthenticationPrincipal User user) {
        List<ChatRequest> requests = chatService.getPendingRequests(user);
        List<ChatRequestResponse> responses = requests.stream()
                .map(ChatRequestResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get chat list for authenticated user.
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<List<ChatResponse>> getChatList(
            @AuthenticationPrincipal User user) {
        List<Chat> chats = chatService.getChatList(user);
        List<ChatResponse> responses = chats.stream()
                .map(chat -> {
                    long unreadCount = chatMessageRepository.countUnreadMessages(chat, user);
                    return ChatResponse.fromEntity(chat, unreadCount);
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get messages for a specific chat (paginated).
     * GET /api/chats/{id}/messages?page=0&size=30
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> getChatMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal User user) {
        Page<ChatMessage> messages = chatService.getChatMessages(id, user, page, size);
        Page<MessageResponse> responses = messages.map(MessageResponse::fromEntity);
        return ResponseEntity.ok(responses);
    }

    /**
     * Send a message in a chat.
     * POST /api/chats/{id}/messages
     */
    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @PathVariable UUID id,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal User user) {
        ChatMessage message = chatService.sendMessage(id, user, request.content());
        MessageResponse response = MessageResponse.fromEntity(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Mark messages as read in a chat.
     * POST /api/chats/{id}/read
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        chatService.markAsRead(id, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Block a user.
     * POST /api/chats/block
     */
    @PostMapping("/block")
    public ResponseEntity<Void> blockUser(
            @Valid @RequestBody BlockUserRequest request,
            @AuthenticationPrincipal User user) {
        chatService.blockUser(user, request.userId());
        return ResponseEntity.ok().build();
    }

    /**
     * Unblock a user.
     * POST /api/chats/unblock
     */
    @PostMapping("/unblock")
    public ResponseEntity<Void> unblockUser(
            @Valid @RequestBody BlockUserRequest request,
            @AuthenticationPrincipal User user) {
        chatService.unblockUser(user, request.userId());
        return ResponseEntity.ok().build();
    }

    /**
     * Report a message.
     * POST /api/chats/reports
     */
    @PostMapping("/reports")
    public ResponseEntity<ChatReportResponse> reportMessage(
            @Valid @RequestBody ReportMessageRequest request,
            @AuthenticationPrincipal User user) {
        ChatReport report = chatService.reportMessage(request.messageId(), user, request.reason());
        ChatReportResponse response = ChatReportResponse.fromEntity(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
