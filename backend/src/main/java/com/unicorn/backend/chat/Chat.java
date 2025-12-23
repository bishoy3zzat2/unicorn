package com.unicorn.backend.chat;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a chat conversation between an investor and a startup.
 * The chat is linked to the startup (not individual owner) to maintain
 * continuity during ownership transfers.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "chats", indexes = {
        @Index(name = "idx_chat_investor", columnList = "investor_id"),
        @Index(name = "idx_chat_startup", columnList = "startup_id"),
        @Index(name = "idx_chat_status", columnList = "status"),
        @Index(name = "idx_chat_last_message", columnList = "last_message_at")
})
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @lombok.EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    @lombok.ToString.Exclude
    private User investor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startup_id", nullable = false)
    @lombok.ToString.Exclude
    private Startup startup;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChatStatus status = ChatStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_id", nullable = false)
    @lombok.ToString.Exclude
    private User initiatedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    @lombok.ToString.Exclude
    private List<ChatMessage> messages = new ArrayList<>();

    @Override
    public String toString() {
        return "Chat(id=" + id + ")";
    }
}
