package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "quiz_badges")
@IdClass(QuizBadge.QuizBadgeId.class)
public class QuizBadge implements Serializable {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    @EqualsAndHashCode.Include
    private Quiz quiz;
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id")
    @EqualsAndHashCode.Include
    private Badge badge;
    @CreationTimestamp
    private LocalDateTime assignedAt;  
    private LocalDateTime expirationDate;
    @Data
    @NoArgsConstructor
    public static class QuizBadgeId implements Serializable {
        private UUID quiz;
        private UUID badge;
    }
}
