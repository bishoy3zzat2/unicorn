package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "course_certificates")
public class CourseCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false, unique = true)
    private Course course;
    @Column(nullable = false, unique = true, length = 255)
    private String slug;  
    @Column(nullable = false, length = 255)
    private String title;  
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(columnDefinition = "TEXT")
    private String requirements;  
    @Column(nullable = false)
    private Integer minCompletionPercentage = 100;  
    @Column(nullable = false)
    private Boolean requiresInterview = false;
    @Column(nullable = false)
    private Boolean requiresSpecialExam = false;
    @Column(columnDefinition = "TEXT")
    private String examRequirements;  
    @Column(length = 500)
    private String templateUrl;  
    @Column(nullable = false)
    private Boolean isActive = true;
    private Integer validityMonths;  
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
