package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_attempts")
public class UserAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Enumerated(EnumType.STRING)  
    @Column(nullable = false, length = 20)
    private AttemptStatus status;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;
    private Integer score; 
    @Column(nullable = false)
    private Boolean isPassed = false;
    @CreationTimestamp
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeTakenSeconds; 
    @Column(columnDefinition = "TEXT")
    private String proctoringLog; 
    private Integer browserLeaveCount = 0; 
    private LocalDateTime dueDate;
    @Column(nullable = false)
    private Boolean allowLateSubmission = false;
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AttemptAnswer> answers;
}