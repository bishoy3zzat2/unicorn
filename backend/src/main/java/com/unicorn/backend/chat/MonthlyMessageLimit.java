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
import java.util.UUID;

/**
 * Entity to track monthly message limits for Elite plan startups.
 * Elite startups can send one introductory message per investor per calendar
 * month.
 * The limit resets on the 1st of each month.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "monthly_message_limits", uniqueConstraints = @UniqueConstraint(name = "uk_monthly_limit", columnNames = {
        "startup_id", "investor_id", "month", "year" }), indexes = {
                @Index(name = "idx_limit_startup", columnList = "startup_id"),
                @Index(name = "idx_limit_investor", columnList = "investor_id"),
                @Index(name = "idx_limit_month_year", columnList = "month,year")
        })
public class MonthlyMessageLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "startup_id", nullable = false)
    private Startup startup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investor_id", nullable = false)
    private User investor;

    @Column(nullable = false)
    private Integer month;

    @Column(nullable = false)
    private Integer year;

    @CreationTimestamp
    @Column(name = "message_sent_at", nullable = false, updatable = false)
    private LocalDateTime messageSentAt;
}
