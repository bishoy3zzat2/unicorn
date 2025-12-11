package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.QuizCreateRequest;
import com.loyalixa.backend.course.dto.QuizResponse;
import com.loyalixa.backend.course.dto.QuizUpdateRequest;
import com.loyalixa.backend.course.dto.QuestionRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class QuizAdminService {
    private final QuizRepository quizRepository;
    private final QuizService quizService;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final BadgeRepository badgeRepository;
    public QuizAdminService(
            QuizRepository quizRepository,
            QuizService quizService,
            QuestionRepository questionRepository,
            LessonRepository lessonRepository,
            BadgeRepository badgeRepository) {
        this.quizRepository = quizRepository;
        this.quizService = quizService;
        this.questionRepository = questionRepository;
        this.lessonRepository = lessonRepository;
        this.badgeRepository = badgeRepository;
    }
    @Transactional(readOnly = true)
    public List<QuizResponse> getAllQuizzes() {
        return quizRepository.findAll().stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByCourse(UUID courseId) {
        List<Quiz> allQuizzes = quizRepository.findAll();
        return allQuizzes.stream()
                .filter(quiz -> {
                    if (quiz.getLesson() != null && 
                        quiz.getLesson().getSection() != null &&
                        quiz.getLesson().getSection().getCourse() != null &&
                        quiz.getLesson().getSection().getCourse().getId().equals(courseId)) {
                        return true;
                    }
                    return false;
                })
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByLesson(Long lessonId) {
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Lesson not found: " + lessonId);
        }
        return quizRepository.findByLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
        return mapToQuizResponse(quiz);
    }
    @Transactional
    public QuizResponse createQuiz(QuizCreateRequest request) {
        Quiz quiz = quizService.createNewQuiz(request);
        if (request.badgeIds() != null && !request.badgeIds().isEmpty()) {
            List<Badge> badges = badgeRepository.findAllById(request.badgeIds());
            quiz.setBadges(new java.util.HashSet<>(badges));
            quiz = quizRepository.save(quiz);
        }
        return mapToQuizResponse(quiz);
    }
    @Transactional
    public QuizResponse updateQuiz(UUID quizId, QuizUpdateRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
        if (request.title() != null) {
            quiz.setTitle(request.title());
        }
        if (request.durationMinutes() != null) {
            quiz.setDurationMinutes(request.durationMinutes());
        }
        if (request.orderIndex() != null) {
            quiz.setOrderIndex(request.orderIndex());
        }
        if (request.passScorePercentage() != null) {
            quiz.setPassScorePercentage(request.passScorePercentage());
        }
        if (request.requiresProctoring() != null) {
            quiz.setRequiresProctoring(request.requiresProctoring());
        }
        if (request.quizType() != null) {
            quiz.setQuizType(request.quizType());
        }
        if (request.maxAttempts() != null) {
            quiz.setMaxAttempts(request.maxAttempts());
        }
        if (request.gradingStrategy() != null) {
            quiz.setGradingStrategy(request.gradingStrategy());
        }
        if (request.gradingType() != null) {
            quiz.setGradingType(request.gradingType());
        }
        if (request.allowLateSubmission() != null) {
            quiz.setAllowLateSubmission(request.allowLateSubmission());
        }
        if (request.requiredDeviceType() != null) {
            quiz.setRequiredDeviceType(request.requiredDeviceType());
        }
        if (request.allowedBrowsers() != null) {
            quiz.setAllowedBrowsers(request.allowedBrowsers());
        }
        if (request.instructions() != null) {
            quiz.setInstructions(request.instructions());
        }
        if (request.lessonId() != null) {
            Lesson lesson = lessonRepository.findById(request.lessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Lesson not found: " + request.lessonId()));
            quiz.setLesson(lesson);
        }
        if (request.badgeIds() != null) {
            if (request.badgeIds().isEmpty()) {
                quiz.setBadges(new java.util.HashSet<>());
            } else {
                List<Badge> badges = badgeRepository.findAllById(request.badgeIds());
                quiz.setBadges(new java.util.HashSet<>(badges));
            }
        }
        if (request.questions() != null && !request.questions().isEmpty()) {
            questionRepository.deleteByQuizId(quizId);
            java.util.Set<Question> newQuestions = new java.util.HashSet<>();
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            for (QuestionRequest qReq : request.questions()) {
                Question question = new Question();
                question.setQuiz(quiz);
                question.setQuestionText(qReq.questionText());
                question.setQuestionType(qReq.questionType());
                question.setPoints(qReq.points());
                question.setOrderIndex(qReq.orderIndex());
                question.setCorrectAnswer(qReq.correctAnswer());
                try {
                    String optionsJson = objectMapper.writeValueAsString(qReq.options());
                    question.setOptionsJson(optionsJson);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid question options: " + e.getMessage());
                }
                newQuestions.add(question);
            }
            quiz.setQuestions(newQuestions);
        }
        Quiz savedQuiz = quizRepository.save(quiz);
        return mapToQuizResponse(savedQuiz);
    }
    @Transactional
    public void deleteQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + quizId));
        lessonRepository.unlinkQuizFromLessons(quizId);
        quizRepository.delete(quiz);
    }
    private QuizResponse mapToQuizResponse(Quiz quiz) {
        int questionsCount = quiz.getQuestions() != null ? quiz.getQuestions().size() : 0;
        Long lessonId = quiz.getLesson() != null ? quiz.getLesson().getId() : null;
        UUID bundleId = quiz.getBundle() != null ? quiz.getBundle().getId() : null;
        List<UUID> badgeIds = null;
        if (quiz.getBadges() != null && !quiz.getBadges().isEmpty()) {
            badgeIds = quiz.getBadges().stream()
                    .map(Badge::getId)
                    .collect(Collectors.toList());
        }
        return new QuizResponse(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDurationMinutes(),
                quiz.getOrderIndex(),
                quiz.getPassScorePercentage(),
                quiz.getRequiresProctoring(),
                quiz.getQuizType(),
                quiz.getMaxAttempts(),
                quiz.getGradingStrategy(),
                quiz.getGradingType(),
                quiz.getAllowLateSubmission(),
                quiz.getRequiredDeviceType(),
                quiz.getAllowedBrowsers(),
                quiz.getWeightPercentage(),
                quiz.getInstructions(),  
                lessonId,
                bundleId,
                questionsCount,
                badgeIds != null ? badgeIds : java.util.Collections.emptyList()  
        );
    }
}
