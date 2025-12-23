package com.unicorn.backend.chat;

import com.unicorn.backend.investor.InvestorProfileRepository;
import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Core service for chat operations.
 * Handles chat creation, messaging, requests, blocking, and reporting.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRequestRepository chatRequestRepository;
    private final ChatBlockRepository chatBlockRepository;
    private final ChatReportRepository chatReportRepository;
    private final MonthlyMessageLimitRepository monthlyMessageLimitRepository;
    private final StartupRepository startupRepository;
    private final UserRepository userRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final ChatPermissionService permissionService;

    /**
     * Start a direct chat (Investor → Startup).
     * Only verified investors can initiate chats directly.
     *
     * @param initiator the investor starting the chat
     * @param startupId the target startup ID
     * @return the created or existing chat
     */
    @Transactional
    public Chat startChat(User initiator, UUID startupId) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Check permissions
        if (!permissionService.canInitiateChat(initiator, startup)) {
            throw new AccessDeniedException("You do not have permission to start this chat");
        }

        // Check if chat already exists
        return chatRepository.findByInvestorAndStartup(initiator, startup)
                .orElseGet(() -> {
                    Chat chat = Chat.builder()
                            .investor(initiator)
                            .startup(startup)
                            .initiatedBy(initiator)
                            .status(ChatStatus.ACTIVE)
                            .lastMessageAt(LocalDateTime.now())
                            .build();
                    return chatRepository.save(chat);
                });
    }

    /**
     * Send a chat request (Elite Startup → Investor).
     * Elite startups can send one introductory message per investor per month.
     *
     * @param startupId      the startup ID
     * @param investorId     the target investor ID
     * @param initialMessage the introductory message
     * @param requester      the user making the request (must be startup owner)
     * @return the created chat request
     */
    @Transactional
    public ChatRequest sendChatRequest(UUID startupId, UUID investorId, String initialMessage, User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        User investor = userRepository.findById(investorId)
                .orElseThrow(() -> new IllegalArgumentException("Investor not found"));

        // Verify requester is the startup owner
        if (!startup.getOwner().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Only the startup owner can send chat requests");
        }

        // Verify investor has investor profile
        if (investorProfileRepository.findByUser(investor).isEmpty()) {
            throw new IllegalArgumentException("Target user is not an investor");
        }

        // Check permissions (Elite plan + monthly limit)
        if (!permissionService.canSendChatRequest(startup, investor)) {
            throw new AccessDeniedException("You cannot send a chat request to this investor. " +
                    "You may have reached your monthly limit or your subscription plan does not allow this.");
        }

        // Create the request
        ChatRequest request = ChatRequest.builder()
                .startup(startup)
                .investor(investor)
                .initialMessage(initialMessage)
                .status(RequestStatus.PENDING)
                .build();

        ChatRequest savedRequest = chatRequestRepository.save(request);

        // Record the monthly limit immediately
        LocalDateTime now = LocalDateTime.now();
        MonthlyMessageLimit limit = MonthlyMessageLimit.builder()
                .startup(startup)
                .investor(investor)
                .month(now.getMonthValue())
                .year(now.getYear())
                .build();
        monthlyMessageLimitRepository.save(limit);

        return savedRequest;
    }

    /**
     * Accept a chat request and create the chat.
     *
     * @param requestId the request ID
     * @param investor  the investor accepting
     * @return the created chat
     */
    @Transactional
    public Chat acceptChatRequest(UUID requestId, User investor) {
        ChatRequest request = chatRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Verify investor is the recipient
        if (!request.getInvestor().getId().equals(investor.getId())) {
            throw new AccessDeniedException("You are not the recipient of this request");
        }

        // Verify request is pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request has already been responded to");
        }

        // Create the chat
        Chat chat = Chat.builder()
                .investor(investor)
                .startup(request.getStartup())
                .initiatedBy(request.getStartup().getOwner())
                .status(ChatStatus.ACTIVE)
                .lastMessageAt(LocalDateTime.now())
                .build();

        Chat savedChat = chatRepository.save(chat);

        // Create the initial message from the request
        ChatMessage initialMsg = ChatMessage.builder()
                .chat(savedChat)
                .sender(request.getStartup().getOwner())
                .content(request.getInitialMessage())
                .isRead(false)
                .build();

        chatMessageRepository.save(initialMsg);

        // Update request status
        request.setStatus(RequestStatus.ACCEPTED);
        request.setRespondedAt(LocalDateTime.now());
        request.setChat(savedChat);
        chatRequestRepository.save(request);

        return savedChat;
    }

    /**
     * Decline a chat request.
     *
     * @param requestId the request ID
     * @param investor  the investor declining
     */
    @Transactional
    public void declineChatRequest(UUID requestId, User investor) {
        ChatRequest request = chatRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Verify investor is the recipient
        if (!request.getInvestor().getId().equals(investor.getId())) {
            throw new AccessDeniedException("You are not the recipient of this request");
        }

        // Verify request is pending
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Request has already been responded to");
        }

        // Update request status
        request.setStatus(RequestStatus.DECLINED);
        request.setRespondedAt(LocalDateTime.now());
        chatRequestRepository.save(request);
    }

    /**
     * Send a message in an existing chat.
     *
     * @param chatId  the chat ID
     * @param sender  the message sender
     * @param content the message content
     * @return the created message
     */
    @Transactional
    public ChatMessage sendMessage(UUID chatId, User sender, String content) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Check permissions
        if (!permissionService.canSendMessage(sender, chat)) {
            throw new AccessDeniedException("You cannot send messages in this chat");
        }

        // Create the message
        ChatMessage message = ChatMessage.builder()
                .chat(chat)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Update chat's last message timestamp
        chat.setLastMessageAt(LocalDateTime.now());
        chatRepository.save(chat);

        return savedMessage;
    }

    /**
     * Get the chat list for a user.
     *
     * @param user the user
     * @return list of chats
     */
    @Transactional(readOnly = true)
    public List<Chat> getChatList(User user) {
        return chatRepository.findAllChatsForUser(user);
    }

    /**
     * Get messages for a chat (paginated).
     *
     * @param chatId    the chat ID
     * @param requester the user requesting (must be a participant)
     * @param page      page number (0-indexed)
     * @param size      page size (default 30)
     * @return page of messages
     */
    @Transactional(readOnly = true)
    public Page<ChatMessage> getChatMessages(UUID chatId, User requester, int page, int size) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Verify requester is a participant
        boolean isParticipant = chat.getInvestor().getId().equals(requester.getId()) ||
                chat.getStartup().getOwner().getId().equals(requester.getId());

        if (!isParticipant) {
            throw new AccessDeniedException("You are not a participant in this chat");
        }

        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findByChatOrderByCreatedAtDesc(chat, pageable);
    }

    /**
     * Mark all messages in a chat as read by the recipient.
     *
     * @param chatId the chat ID
     * @param reader the user marking as read
     */
    @Transactional
    public void markAsRead(UUID chatId, User reader) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Get all unread messages not sent by the reader
        Pageable pageable = PageRequest.of(0, 1000); // Get a large batch
        Page<ChatMessage> messages = chatMessageRepository.findByChatOrderByCreatedAtDesc(chat, pageable);

        LocalDateTime now = LocalDateTime.now();
        messages.getContent().stream()
                .filter(m -> !m.getSender().getId().equals(reader.getId()))
                .filter(m -> !m.getIsRead())
                .filter(m -> !m.getIsDeleted())
                .forEach(m -> {
                    m.setIsRead(true);
                    m.setReadAt(now);
                });

        chatMessageRepository.saveAll(messages.getContent());
    }

    /**
     * Block a user.
     *
     * @param blocker   the user doing the blocking
     * @param blockedId the ID of the user to block
     */
    @Transactional
    public void blockUser(User blocker, UUID blockedId) {
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if already blocked
        if (chatBlockRepository.existsByBlockerAndBlocked(blocker, blocked)) {
            return; // Already blocked
        }

        ChatBlock block = ChatBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build();

        chatBlockRepository.save(block);
    }

    /**
     * Unblock a user.
     *
     * @param blocker   the user who blocked
     * @param blockedId the ID of the user to unblock
     */
    @Transactional
    public void unblockUser(User blocker, UUID blockedId) {
        User blocked = userRepository.findById(blockedId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        chatBlockRepository.findByBlockerAndBlocked(blocker, blocked)
                .ifPresent(chatBlockRepository::delete);
    }

    /**
     * Report a message.
     *
     * @param messageId the message ID
     * @param reporter  the user reporting
     * @param reason    the reason for the report
     * @return the created report
     */
    @Transactional
    public ChatReport reportMessage(UUID messageId, User reporter, String reason) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));

        // Don't allow reporting own messages
        if (message.getSender().getId().equals(reporter.getId())) {
            throw new IllegalArgumentException("You cannot report your own message");
        }

        ChatReport report = ChatReport.builder()
                .message(message)
                .reporter(reporter)
                .reason(reason)
                .status(ReportStatus.PENDING)
                .build();

        return chatReportRepository.save(report);
    }

    /**
     * Get pending chat requests for an investor.
     *
     * @param investor the investor
     * @return list of pending requests
     */
    @Transactional(readOnly = true)
    public List<ChatRequest> getPendingRequests(User investor) {
        return chatRequestRepository.findByInvestorAndStatusOrderByCreatedAtDesc(investor, RequestStatus.PENDING);
    }
}
