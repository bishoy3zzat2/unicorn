package com.loyalixa.backend.lxcoins;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "lxcoins_reward_configs")
public class LXCoinsRewardConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    @Column(name = "activity_type", nullable = false, unique = true, length = 50)
    private String activityType;
    @Column(name = "base_reward", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseReward = BigDecimal.ZERO;
    @Column(nullable = false)
    private Boolean isEnabled = true;
    @Column(columnDefinition = "TEXT")
    private String configJson;
    @Column(columnDefinition = "TEXT")
    private String description;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
