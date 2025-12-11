package com.loyalixa.backend.course;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@Entity
@Table(name = "lessons")
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private Boolean isFreePreview = false;
    @Column(nullable = false, length = 50)
    private String contentType;  
    @Column(columnDefinition = "TEXT")
    private String contentBody;
    private String contentUrl;  
    private Integer durationInSeconds;
    @Column(nullable = false)
    private Integer orderIndex;
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id")
    private Quiz quiz;
    @Column(columnDefinition = "TEXT", name = "session_details")
    private String sessionDetails;  
    @Column(columnDefinition = "TEXT", name = "assessment_info")
    private String assessmentInfo;  
    @Column(name = "video_thumbnail_url")
    private String videoThumbnailUrl;
    @Column(name = "prevent_skip")
    private Boolean preventSkip = false;
    @Column(name = "downloadable")
    private Boolean downloadable = false;
    @Column(name = "allow_speed_control")
    private Boolean allowSpeedControl = true;
    @Column(name = "require_fullscreen_for_progress")
    private Boolean requireFullscreenForProgress = false;
    @Column(name = "completion_percentage")
    private Integer completionPercentage = 90;  
    @Column(name = "require_prerequisites")
    private Boolean requirePrerequisites = false;
    @Column(columnDefinition = "TEXT", name = "prerequisite_lesson_ids")
    private String prerequisiteLessonIds;  
    @Column(columnDefinition = "TEXT", name = "video_transcript")
    private String videoTranscript;
    @Column(columnDefinition = "TEXT", name = "subtitles")
    private String subtitles;  
    @Column(name = "allow_ads")
    private Boolean allowAds = false;
    @Column(name = "show_ads_for_free_only")
    private Boolean showAdsForFreeOnly = true;
    @Column(name = "ad_mode", length = 20)
    private String adMode;  
    @Column(name = "ad_tag_url")
    private String adTagUrl;
    @Column(columnDefinition = "TEXT", name = "ad_breaks")
    private String adBreaks;  
    @Column(name = "ad_skip_after_seconds")
    private Integer adSkipAfterSeconds;  
    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Quiz> quizzes;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private CourseSection section;
}