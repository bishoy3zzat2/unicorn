package com.loyalixa.backend.course;

import com.loyalixa.backend.course.dto.QuizCreateRequest;
import com.loyalixa.backend.course.dto.AttemptResponse;
import com.loyalixa.backend.course.dto.DeviceCheckRequest;
import com.loyalixa.backend.course.dto.QuizSubmissionRequest;
import com.loyalixa.backend.course.dto.QuestionRequest;
import com.loyalixa.backend.course.dto.SubmissionResponse;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserActivityLogRepository;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final LessonRepository lessonRepository;
    private final UserActivityLogRepository userActivityLogRepository;
    private final UserAttemptRepository userAttemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;

    public QuizService(QuizRepository quizRepository,
            QuestionRepository questionRepository,
            LessonRepository lessonRepository,
            UserActivityLogRepository userActivityLogRepository,
            UserAttemptRepository userAttemptRepository,
            AttemptAnswerRepository attemptAnswerRepository,
            CourseRepository courseRepository,
            CategoryRepository categoryRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.lessonRepository = lessonRepository;
        this.userActivityLogRepository = userActivityLogRepository;
        this.userAttemptRepository = userAttemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Quiz createNewQuiz(QuizCreateRequest request) {

        if (request.lessonId() == null) {
            throw new IllegalArgumentException("Lesson ID is required. Quiz must be linked to a lesson.");
        }

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + request.lessonId()));

        if (request.badgeIds() != null && !request.badgeIds().isEmpty()) {
            // سيتم ربط الـ badges في QuizAdminService بعد الحفظ
        }

        Quiz newQuiz = new Quiz();
        newQuiz.setTitle(request.title());
        newQuiz.setOrderIndex(request.orderIndex());
        newQuiz.setDurationMinutes(request.durationMinutes());
        newQuiz.setPassScorePercentage(request.passScorePercentage());
        newQuiz.setRequiresProctoring(request.requiresProctoring());
        newQuiz.setQuizType(request.quizType());
        newQuiz.setMaxAttempts(request.maxAttempts());
        newQuiz.setGradingStrategy(request.gradingStrategy());
        newQuiz.setGradingType(request.gradingType());
        newQuiz.setAllowLateSubmission(request.allowLateSubmission());
        newQuiz.setRequiredDeviceType(request.requiredDeviceType());
        newQuiz.setAllowedBrowsers(request.allowedBrowsers());
        if (request.instructions() != null) {
            newQuiz.setInstructions(request.instructions());
        }

        if (request.questions() != null && !request.questions().isEmpty()) {
            Set<Question> questions = processQuestions(request.questions(), newQuiz);
            newQuiz.setQuestions(questions);
        } else {
            throw new IllegalArgumentException("Quiz must contain at least one question.");
        }

        newQuiz.setLesson(lesson);

        return quizRepository.save(newQuiz);
    }

    @Transactional
    public AttemptResponse startNewAttempt(UUID quizId, User student, DeviceCheckRequest deviceCheckRequest) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found with ID: " + quizId));

        if (quiz.getRequiredDeviceType() != null && !"ANY".equalsIgnoreCase(quiz.getRequiredDeviceType())) {

            if ("DESKTOP_ONLY".equalsIgnoreCase(quiz.getRequiredDeviceType())
                    && deviceCheckRequest.screenWidth() < 1024) {
                throw new IllegalStateException("This exam must be taken on a desktop device (min width 1024px).");
            }

            if ("MOBILE_ONLY".equalsIgnoreCase(quiz.getRequiredDeviceType())
                    && deviceCheckRequest.screenWidth() > 768) {
                throw new IllegalStateException("This exam must be taken on a mobile device.");
            }

        }

        long completedAttempts = userAttemptRepository.countByQuizAndStatusIn(
                quiz, List.of(AttemptStatus.COMPLETED, AttemptStatus.PASSED, AttemptStatus.FAILED));

        if (completedAttempts >= quiz.getMaxAttempts()) {
            throw new IllegalStateException(
                    "You have reached the maximum allowed attempts (" + quiz.getMaxAttempts() + ") for this quiz.");
        }

        UserAttempt newAttempt = new UserAttempt();
        newAttempt.setQuiz(quiz);
        newAttempt.setStudent(student);
        newAttempt.setStatus(AttemptStatus.IN_PROGRESS);
        newAttempt.setAllowLateSubmission(quiz.getAllowLateSubmission()); // هذا الحقل غير موجود بعد
        UserAttempt savedAttempt = userAttemptRepository.save(newAttempt);

        return new AttemptResponse(
                savedAttempt.getId(),
                savedAttempt.getQuiz().getId(),
                savedAttempt.getStudent().getId(),
                savedAttempt.getStatus(),
                savedAttempt.getStartedAt());
    }

    @Transactional
    public SubmissionResponse submitQuizAttempt(User student, QuizSubmissionRequest submission) {

        UserAttempt attempt = userAttemptRepository.findById(submission.userAttemptId())
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found or invalid ID."));

        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new IllegalStateException("Unauthorized: Submission is not from the attempt owner.");
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new IllegalStateException("Quiz is not currently in progress or already completed.");
        }

        int totalScore = 0;
        int totalMaxScore = 0;

        for (com.loyalixa.backend.course.dto.AnswerSubmissionRequest answerReq : submission.answers()) {
            Question question = questionRepository.findById(answerReq.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Question not found."));

            AttemptAnswer attemptAnswer = new AttemptAnswer();
            attemptAnswer.setAttempt(attempt);
            attemptAnswer.setQuestion(question);
            attemptAnswer.setStudentAnswer(answerReq.studentAnswer());
            attemptAnswer.setTimeToAnswerSeconds(answerReq.timeToAnswerSeconds());

            totalMaxScore += question.getPoints();

            if (question.getCorrectAnswer() != null &&
                    question.getCorrectAnswer().equalsIgnoreCase(answerReq.studentAnswer().trim())) {
                attemptAnswer.setScoreAchieved(question.getPoints());
                totalScore += question.getPoints();
            } else {
                attemptAnswer.setScoreAchieved(0);
            }

            attemptAnswer.setIsCopied(answerReq.isCopied());
            attemptAnswer.setScreenCaptured(answerReq.screenCaptured());

            attemptAnswerRepository.save(attemptAnswer);
        }

        attempt.setScore(totalScore);
        attempt.setTimeTakenSeconds(submission.timeTakenSeconds());
        attempt.setBrowserLeaveCount(submission.browserLeaveCount());
        attempt.setProctoringLog(submission.proctoringDetails());

        double finalPercentage = (totalMaxScore > 0) ? ((double) totalScore / totalMaxScore) * 100 : 0;
        int passScore = attempt.getQuiz().getPassScorePercentage();

        boolean passed = finalPercentage >= passScore;

        if (passed) {
            attempt.setStatus(AttemptStatus.PASSED);
            attempt.setIsPassed(true);
        } else {
            attempt.setStatus(AttemptStatus.FAILED);
            attempt.setIsPassed(false);
        }

        if ("PASS_FAIL".equalsIgnoreCase(attempt.getQuiz().getGradingType())) {
            attempt.setScore(passed ? 1 : 0);
        } else {
            attempt.setScore(totalScore);
        }

        attempt.setCompletedAt(java.time.LocalDateTime.now());

        UserAttempt savedAttempt = userAttemptRepository.save(attempt);

        return new SubmissionResponse(
                savedAttempt.getId(),
                savedAttempt.getQuiz().getId(),
                savedAttempt.getScore(),
                savedAttempt.getIsPassed(),
                savedAttempt.getStatus(),
                savedAttempt.getBrowserLeaveCount());
    }

    private Set<Question> processQuestions(List<QuestionRequest> questionRequests, Quiz quiz) {
        Set<Question> questions = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (QuestionRequest req : questionRequests) {
            Question question = new Question();
            question.setQuiz(quiz);

            question.setQuestionText(req.questionText());
            question.setQuestionType(req.questionType());
            question.setPoints(req.points());
            question.setOrderIndex(req.orderIndex());

            try {
                String optionsJson = objectMapper.writeValueAsString(req.options());
                question.setOptionsJson(optionsJson);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Invalid format for question options: " + e.getMessage());
            }

            question.setCorrectAnswer(req.correctAnswer());

            questions.add(question);
        }
        return questions;
    }
}