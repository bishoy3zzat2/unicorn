package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "course_id"}) 
})
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;  
    @Column(nullable = false)
    private String paymentStatus;  
    @Column(nullable = false)
    private String enrollmentSource;  
    @CreationTimestamp
    private LocalDateTime enrollmentDate;
    @Column(nullable = true)  
    private LocalDateTime startDate;  
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EnrollmentSource source = EnrollmentSource.PURCHASE;
    @Column(nullable = false, length = 20)
    private String enrollmentStatus = "ACTIVE";  
}