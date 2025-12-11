package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class LessonAdminService {
    private final LessonRepository lessonRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    public LessonAdminService(
            LessonRepository lessonRepository,
            CourseSectionRepository courseSectionRepository,
            QuizRepository quizRepository,
            CourseRepository courseRepository
    ) {
        this.lessonRepository = lessonRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.quizRepository = quizRepository;
        this.courseRepository = courseRepository;
    }
    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsBySection(Long sectionId) {
        if (!courseSectionRepository.existsById(sectionId)) {
            throw new IllegalArgumentException("Section not found: " + sectionId);
        }
        List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(sectionId);
        return lessons.stream()
                .map(lesson -> mapToLessonResponse(lesson))
                .collect(Collectors.toList());
    }
    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        CourseSection section = courseSectionRepository.findById(request.sectionId())
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + request.sectionId()));
        validateContentType(request.contentType());
        Integer orderIndex = request.orderIndex();
        if (orderIndex == null) {
            Integer maxOrder = lessonRepository.findMaxOrderIndexBySectionId(request.sectionId());
            orderIndex = maxOrder != null ? maxOrder + 1 : 1;
        }
        Lesson lesson = new Lesson();
        lesson.setTitle(request.title());
        lesson.setOrderIndex(orderIndex);
        lesson.setContentType(request.contentType().toUpperCase());
        lesson.setIsFreePreview(request.isFreePreview() != null ? request.isFreePreview() : false);
        lesson.setSection(section);
        setContentFields(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        if (savedLesson.getIsFreePreview()) {
            Course course = section.getCourse();
            course.setHasFreeContent(true);
            courseRepository.save(course);
        }
        return mapToLessonResponse(savedLesson);
    }
    @Transactional
    public LessonResponse updateLesson(Long lessonId, LessonUpdateRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));
        if (request.title() != null) {
            lesson.setTitle(request.title());
        }
        if (request.orderIndex() != null) {
            lesson.setOrderIndex(request.orderIndex());
        }
        if (request.contentType() != null) {
            validateContentType(request.contentType());
            lesson.setContentType(request.contentType().toUpperCase());
        }
        if (request.isFreePreview() != null) {
            lesson.setIsFreePreview(request.isFreePreview());
        }
        if (request.expiryDate() != null) {
            lesson.setExpiryDate(request.expiryDate());
        }
        updateContentFields(lesson, request);
        Lesson savedLesson = lessonRepository.save(lesson);
        CourseSection section = savedLesson.getSection();
        Course course = section.getCourse();
        boolean hasFreeContent = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section.getId())
                .stream()
                .anyMatch(l -> l.getIsFreePreview()) || 
                courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(course.getId())
                .stream()
                .anyMatch(s -> s.getIsFreePreview());
        course.setHasFreeContent(hasFreeContent);
        courseRepository.save(course);
        return mapToLessonResponse(savedLesson);
    }
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + lessonId));
        CourseSection section = lesson.getSection();
        Course course = section.getCourse();
        lessonRepository.delete(lesson);
        boolean hasFreeContent = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section.getId())
                .stream()
                .anyMatch(l -> l.getIsFreePreview()) || 
                courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(course.getId())
                .stream()
                .anyMatch(s -> s.getIsFreePreview());
        course.setHasFreeContent(hasFreeContent);
        courseRepository.save(course);
    }
    @Transactional
    public List<LessonResponse> updateLessonsOrder(LessonOrderUpdateRequest request) {
        if (!courseSectionRepository.existsById(request.sectionId())) {
            throw new IllegalArgumentException("Section not found: " + request.sectionId());
        }
        for (LessonOrderUpdateRequest.LessonOrderItem item : request.lessons()) {
            Lesson lesson = lessonRepository.findById(item.lessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + item.lessonId()));
            if (!lesson.getSection().getId().equals(request.sectionId())) {
                throw new IllegalArgumentException("Lesson " + item.lessonId() + 
                        " does not belong to section " + request.sectionId());
            }
            lesson.setOrderIndex(item.orderIndex());
            lessonRepository.save(lesson);
        }
        return getLessonsBySection(request.sectionId());
    }
    private void validateContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be empty.");
        }
        String normalized = contentType.toUpperCase();
        List<String> validTypes = List.of("VIDEO", "ARTICLE", "QUIZ", "MATERIAL", "LIVE_SESSION");
        if (!validTypes.contains(normalized)) {
            throw new IllegalArgumentException("Invalid content type: " + contentType + 
                    ". Valid types are: " + String.join(", ", validTypes));
        }
    }
    private void setContentFields(Lesson lesson, LessonRequest request) {
        String contentType = lesson.getContentType();
        switch (contentType) {
            case "VIDEO":
                lesson.setContentUrl(request.contentUrl());
                lesson.setDurationInSeconds(request.durationInSeconds());
                if (request.videoThumbnailUrl() != null) {
                    lesson.setVideoThumbnailUrl(request.videoThumbnailUrl());
                }
                if (request.preventSkip() != null) {
                    lesson.setPreventSkip(request.preventSkip());
                }
                if (request.downloadable() != null) {
                    lesson.setDownloadable(request.downloadable());
                }
                if (request.allowSpeedControl() != null) {
                    lesson.setAllowSpeedControl(request.allowSpeedControl());
                }
                if (request.requireFullscreenForProgress() != null) {
                    lesson.setRequireFullscreenForProgress(request.requireFullscreenForProgress());
                }
                if (request.completionPercentage() != null) {
                    lesson.setCompletionPercentage(request.completionPercentage());
                }
                if (request.requirePrerequisites() != null) {
                    lesson.setRequirePrerequisites(request.requirePrerequisites());
                }
                if (request.prerequisiteLessonIds() != null) {
                    lesson.setPrerequisiteLessonIds(request.prerequisiteLessonIds());
                }
                if (request.videoTranscript() != null) {
                    lesson.setVideoTranscript(request.videoTranscript());
                }
                if (request.subtitles() != null) {
                    lesson.setSubtitles(request.subtitles());
                }
                if (request.allowAds() != null) {
                    lesson.setAllowAds(request.allowAds());
                }
                if (request.showAdsForFreeOnly() != null) {
                    lesson.setShowAdsForFreeOnly(request.showAdsForFreeOnly());
                }
                if (request.adMode() != null) {
                    lesson.setAdMode(request.adMode());
                }
                if (request.adTagUrl() != null) {
                    lesson.setAdTagUrl(request.adTagUrl());
                }
                if (request.adBreaks() != null) {
                    lesson.setAdBreaks(request.adBreaks());
                }
                if (request.adSkipAfterSeconds() != null) {
                    lesson.setAdSkipAfterSeconds(request.adSkipAfterSeconds());
                }
                break;
            case "ARTICLE":
                lesson.setContentBody(request.contentBody());
                lesson.setContentUrl(request.contentUrl());  
                break;
            case "MATERIAL":
                lesson.setContentUrl(request.contentUrl());
                break;
            case "QUIZ":
                if (request.quizId() != null) {
                    Quiz quiz = quizRepository.findById(request.quizId())
                            .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + request.quizId()));
                    lesson.setQuiz(quiz);
                } else {
                    throw new IllegalArgumentException("Quiz ID is required for QUIZ content type.");
                }
                break;
            case "LIVE_SESSION":
                lesson.setSessionDetails(request.sessionDetails());
                lesson.setAssessmentInfo(request.assessmentInfo());
                lesson.setContentBody(request.contentBody());  
                if (request.contentUrl() != null) {
                    lesson.setContentUrl(request.contentUrl());  
                }
                break;
        }
        lesson.setExpiryDate(request.expiryDate());
    }
    private void updateContentFields(Lesson lesson, LessonUpdateRequest request) {
        String contentType = lesson.getContentType();
        if (request.contentType() != null && !request.contentType().equals(contentType)) {
            lesson.setContentUrl(null);
            lesson.setContentBody(null);
            lesson.setQuiz(null);
            lesson.setSessionDetails(null);
            lesson.setAssessmentInfo(null);
            contentType = request.contentType().toUpperCase();
        }
        switch (contentType) {
            case "VIDEO":
                if (request.contentUrl() != null) {
                    lesson.setContentUrl(request.contentUrl());
                }
                if (request.durationInSeconds() != null) {
                    lesson.setDurationInSeconds(request.durationInSeconds());
                }
                if (request.videoThumbnailUrl() != null) {
                    lesson.setVideoThumbnailUrl(request.videoThumbnailUrl());
                }
                if (request.preventSkip() != null) {
                    lesson.setPreventSkip(request.preventSkip());
                }
                if (request.downloadable() != null) {
                    lesson.setDownloadable(request.downloadable());
                }
                if (request.allowSpeedControl() != null) {
                    lesson.setAllowSpeedControl(request.allowSpeedControl());
                }
                if (request.requireFullscreenForProgress() != null) {
                    lesson.setRequireFullscreenForProgress(request.requireFullscreenForProgress());
                }
                if (request.completionPercentage() != null) {
                    lesson.setCompletionPercentage(request.completionPercentage());
                }
                if (request.requirePrerequisites() != null) {
                    lesson.setRequirePrerequisites(request.requirePrerequisites());
                }
                if (request.prerequisiteLessonIds() != null) {
                    lesson.setPrerequisiteLessonIds(request.prerequisiteLessonIds());
                }
                if (request.videoTranscript() != null) {
                    lesson.setVideoTranscript(request.videoTranscript());
                }
                if (request.subtitles() != null) {
                    lesson.setSubtitles(request.subtitles());
                }
                if (request.allowAds() != null) {
                    lesson.setAllowAds(request.allowAds());
                }
                if (request.showAdsForFreeOnly() != null) {
                    lesson.setShowAdsForFreeOnly(request.showAdsForFreeOnly());
                }
                if (request.adMode() != null) {
                    lesson.setAdMode(request.adMode());
                }
                if (request.adTagUrl() != null) {
                    lesson.setAdTagUrl(request.adTagUrl());
                }
                if (request.adBreaks() != null) {
                    lesson.setAdBreaks(request.adBreaks());
                }
                if (request.adSkipAfterSeconds() != null) {
                    lesson.setAdSkipAfterSeconds(request.adSkipAfterSeconds());
                }
                break;
            case "ARTICLE":
                if (request.contentBody() != null) {
                    lesson.setContentBody(request.contentBody());
                }
                if (request.contentUrl() != null) {
                    lesson.setContentUrl(request.contentUrl());
                }
                break;
            case "MATERIAL":
                if (request.contentUrl() != null) {
                    lesson.setContentUrl(request.contentUrl());
                }
                break;
            case "QUIZ":
                if (request.quizId() != null) {
                    Quiz quiz = quizRepository.findById(request.quizId())
                            .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + request.quizId()));
                    lesson.setQuiz(quiz);
                }
                break;
            case "LIVE_SESSION":
                if (request.sessionDetails() != null) {
                    lesson.setSessionDetails(request.sessionDetails());
                }
                if (request.assessmentInfo() != null) {
                    lesson.setAssessmentInfo(request.assessmentInfo());
                }
                if (request.contentBody() != null) {
                    lesson.setContentBody(request.contentBody());
                }
                if (request.contentUrl() != null) {
                    lesson.setContentUrl(request.contentUrl());
                }
                break;
        }
    }
    private LessonResponse mapToLessonResponse(Lesson lesson) {
        CourseSection section = lesson.getSection();
        Course course = section.getCourse();
        UUID quizId = null;
        String quizTitle = null;
        if (lesson.getQuiz() != null) {
            quizId = lesson.getQuiz().getId();
            quizTitle = lesson.getQuiz().getTitle();
        }
        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getOrderIndex(),
                lesson.getContentType(),
                lesson.getIsFreePreview(),
                lesson.getContentUrl(),
                lesson.getContentBody(),
                lesson.getDurationInSeconds(),
                quizId,
                quizTitle,
                lesson.getSessionDetails(),
                lesson.getAssessmentInfo(),
                lesson.getExpiryDate(),
                section.getId(),
                section.getTitle(),
                course.getId(),
                lesson.getVideoThumbnailUrl(),
                lesson.getPreventSkip(),
                lesson.getDownloadable(),
                lesson.getAllowSpeedControl(),
                lesson.getRequireFullscreenForProgress(),
                lesson.getCompletionPercentage(),
                lesson.getRequirePrerequisites(),
                lesson.getPrerequisiteLessonIds(),
                lesson.getVideoTranscript(),
                lesson.getSubtitles(),
                lesson.getAllowAds(),
                lesson.getShowAdsForFreeOnly(),
                lesson.getAdMode(),
                lesson.getAdTagUrl(),
                lesson.getAdBreaks(),
                lesson.getAdSkipAfterSeconds()
        );
    }
}
