package com.unicorn.backend.chat;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Chat entity operations.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * Find all chats for a specific investor.
     */
    List<Chat> findByInvestorOrderByLastMessageAtDesc(User investor);

    /**
     * Find all chats for a specific startup.
     */
    List<Chat> findByStartupOrderByLastMessageAtDesc(Startup startup);

    /**
     * Find a specific chat between an investor and a startup.
     */
    Optional<Chat> findByInvestorAndStartup(User investor, Startup startup);

    /**
     * Check if an active chat exists between an investor and a startup.
     */
    boolean existsByInvestorAndStartupAndStatus(User investor, Startup startup, ChatStatus status);

    /**
     * Find all chats for a user (either as investor or as startup owner).
     * This is used for displaying user's chat list.
     */
    @Query("SELECT c FROM Chat c WHERE c.investor = :user OR c.startup.owner = :user ORDER BY c.lastMessageAt DESC")
    List<Chat> findAllChatsForUser(@Param("user") User user);

    /**
     * Count unread messages for a specific user across all their chats.
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m JOIN m.chat c " +
            "WHERE (c.investor = :user OR c.startup.owner = :user) " +
            "AND m.sender != :user AND m.isRead = false AND m.isDeleted = false")
    long countUnreadMessagesForUser(@Param("user") User user);
}
