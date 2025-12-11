package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "course_bundles")
public class CourseBundle {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String title;
    @Column(nullable = false)
    private BigDecimal price;  
    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quiz> finalExams;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "bundle_courses",  
        joinColumns = @JoinColumn(name = "bundle_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses; 
}