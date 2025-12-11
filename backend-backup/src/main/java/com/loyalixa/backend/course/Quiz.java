package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;  
import lombok.EqualsAndHashCode;  
import lombok.NoArgsConstructor;
import java.util.Set;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Data
@NoArgsConstructor
@Entity
@Table(name = "quizzes")
@EqualsAndHashCode(exclude = {"questions"})  
@ToString(exclude = {"questions"})  
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private Integer durationMinutes;  
    @Column(nullable = false)
    private Integer orderIndex;  
    @Column(nullable = false)
    private Integer passScorePercentage;  
    @Column(nullable = false)
    private Boolean requiresProctoring = false;  
    @Column(nullable = false)
    private String quizType;  
    @Column(nullable = false)
    private Integer maxAttempts = 1;  
    @Column(nullable = false)
    private String gradingStrategy = "HIGHEST_SCORE";  
    @Column(nullable = false)
    private String gradingType = "GRADED";  
    @Column(length = 50)
    private String requiredDeviceType;  
    private String allowedBrowsers;  
    @Column(nullable = false)
    private Double weightPercentage = 0.0;  
    @Column(nullable = false)
    private Boolean allowLateSubmission = false;
    @Column(columnDefinition = "TEXT")
    private String instructions;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson; 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id")
    private CourseBundle bundle; 
    @JsonIgnore
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "quiz_badges",  
        joinColumns = @JoinColumn(name = "quiz_id"),
        inverseJoinColumns = @JoinColumn(name = "badge_id")
    )
    private Set<Badge> badges;
}