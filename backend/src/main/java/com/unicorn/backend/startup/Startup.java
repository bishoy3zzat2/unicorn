package com.unicorn.backend.startup;

import com.unicorn.backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a startup in the Unicorn platform.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "startups")
public class Startup {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 80)
    @jakarta.validation.constraints.Size(max = 80, message = "Tagline must be less than 80 characters")
    private String tagline;

    @Column(length = 200)
    @jakarta.validation.constraints.Size(max = 200, message = "Description must be less than 200 characters")
    private String fullDescription;

    @Column
    private String industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Stage stage;

    @Column(precision = 15, scale = 2)
    private BigDecimal fundingGoal;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    @Column
    private String websiteUrl;

    @Column
    private String logoUrl;

    @Column
    private String coverUrl;

    @Column
    private String facebookUrl;

    @Column
    private String instagramUrl;

    @Column
    private String twitterUrl;

    @Column
    private String pitchDeckUrl;

    @Column
    private String businessPlanUrl;

    @Column
    private String businessModelUrl;

    @Column
    private String financialDocumentsUrl;

    @Enumerated(EnumType.STRING)
    private StartupRole ownerRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StartupStatus status = StartupStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "startup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private java.util.List<StartupMember> members = new java.util.ArrayList<>();

    @Column(name = "warning_count")
    @Builder.Default
    private Integer warningCount = 0;

    @OneToMany(mappedBy = "startup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<StartupModerationLog> moderationLogs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
