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
@Table(name = "course_prerequisites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"course_id", "prerequisite_type", "prerequisite_id", "requirement_type"})
})
public class CoursePrerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    @Column(nullable = false, length = 20)
    private String prerequisiteType;  
    @Column(nullable = false)
    private String prerequisiteId;  
    @Column(nullable = false, length = 20)
    private String requirementType;  
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
