package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "course_reviews", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "user_id"}) 
})
public class CourseReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  
    @Column(nullable = false)
    private Integer rating;  
    @Column(columnDefinition = "TEXT")
    private String comment;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";  
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private Boolean isFeatured = false;  
}