package com.unicorn.backend.nudge;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a nudge from a startup owner to an investor.
 * Nudges are limited based on subscription plan.
 * Each nudge is associated with a specific startup that the sender wants the
 * investor to check.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nudges", indexes = {
        @Index(name = "idx_nudge_sender", columnList = "sender_id"),
        @Index(name = "idx_nudge_receiver", columnList = "receiver_id"),
        @Index(name = "idx_nudge_sender_receiver", columnList = "sender_id, receiver_id"),
        @Index(name = "idx_nudge_startup", columnList = "startup_id"),
        @Index(name = "idx_nudge_created_at", columnList = "created_at")
})
public class Nudge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startup_id", nullable = false)
    private Startup startup;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
