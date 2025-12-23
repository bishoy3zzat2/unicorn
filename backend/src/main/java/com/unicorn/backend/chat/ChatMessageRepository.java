package com.unicorn.backend.chat;

import com.unicorn.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ChatMessage entity operations.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Find messages for a specific chat, ordered by creation time (newest first).
     * Returns paginated results (e.g., 30 messages per page).
     */
    Page<ChatMessage> findByChatOrderByCreatedAtDesc(Chat chat, Pageable pageable);

    /**
     * Count unread messages in a specific chat for a recipient.
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.chat = :chat " +
            "AND m.sender != :recipient AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessages(@Param("chat") Chat chat, @Param("recipient") User recipient);

    /**
     * Find all non-deleted messages in a chat (for admin viewing).
     */
    List<ChatMessage> findByChatAndIsDeletedFalseOrderByCreatedAtAsc(Chat chat);

    /**
     * Find all messages in a chat including deleted ones (for admin/moderation).
     */
    List<ChatMessage> findByChatOrderByCreatedAtAsc(Chat chat);
}
