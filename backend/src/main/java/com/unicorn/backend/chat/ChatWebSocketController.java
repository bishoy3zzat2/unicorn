package com.unicorn.backend.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * WebSocket controller for real-time chat messaging.
 * Handles incoming WebSocket messages and broadcasts them to recipients.
 */
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle incoming chat messages via WebSocket.
     * Messages are sent to /app/chat.send and broadcast to /topic/chat/{chatId}
     *
     * @param message        the message payload
     * @param headerAccessor WebSocket headers
     * @param principal      authenticated user
     */
    @MessageMapping("/chat.send")
    public void sendMessage(
            @Payload WebSocketChatMessage message,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        // Broadcast message to all subscribers of this chat
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.chatId(),
                message);
    }

    /**
     * Handle typing indicator.
     * Messages are sent to /app/chat.typing and broadcast to
     * /topic/chat/{chatId}/typing
     *
     * @param typing    the typing indicator payload
     * @param principal authenticated user
     */
    @MessageMapping("/chat.typing")
    public void handleTyping(
            @Payload TypingIndicator typing,
            Principal principal) {
        // Broadcast typing indicator to all subscribers
        messagingTemplate.convertAndSend(
                "/topic/chat/" + typing.chatId() + "/typing",
                typing);
    }

    /**
     * WebSocket message payload for chat messages.
     */
    public record WebSocketChatMessage(
            UUID chatId,
            UUID messageId,
            UUID senderId,
            String senderName,
            String content,
            String timestamp) {
    }

    /**
     * Typing indicator payload.
     */
    public record TypingIndicator(
            UUID chatId,
            UUID userId,
            String userName,
            boolean isTyping) {
    }
}
