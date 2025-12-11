package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "badge_ranking_weights", uniqueConstraints = {
    @UniqueConstraint(columnNames = "badge_id")
})
public class BadgeRankingWeight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", nullable = false, unique = true)
    private Badge badge;
    @Column(nullable = true)
    private Double weightValue;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
