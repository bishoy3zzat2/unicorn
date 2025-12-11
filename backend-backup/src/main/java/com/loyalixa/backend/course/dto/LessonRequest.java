package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;
public record LessonRequest(
    @NotNull(message = "Section ID is required.")
    Long sectionId,
    @NotBlank(message = "Lesson title cannot be empty.")
    @Size(max = 255, message = "Lesson title must be less than 255 characters.")
    String title,
    Integer orderIndex,
    @NotBlank(message = "Content type is required.")
    String contentType,  
    Boolean isFreePreview,
    String contentUrl,  
    String contentBody,  
    Integer durationInSeconds,  
    UUID quizId,  
    String sessionDetails,  
    String assessmentInfo,  
    LocalDateTime expiryDate,
    String videoThumbnailUrl,  
    Boolean preventSkip,  
    Boolean downloadable,  
    Boolean allowSpeedControl,  
    Boolean requireFullscreenForProgress,  
    Integer completionPercentage,  
    Boolean requirePrerequisites,  
    String prerequisiteLessonIds,  
    String videoTranscript,  
    String subtitles,  
    Boolean allowAds,
    Boolean showAdsForFreeOnly,
    String adMode,  
    String adTagUrl,
    String adBreaks,  
    Integer adSkipAfterSeconds
) {}
