package com.loyalixa.backend.course;

import com.loyalixa.backend.course.dto.AttemptResponse;
import com.loyalixa.backend.course.dto.QuizSubmissionRequest;
import com.loyalixa.backend.course.dto.SubmissionResponse;
import com.loyalixa.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.loyalixa.backend.course.dto.DeviceCheckRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * [STUDENT] بدء محاولة جديدة في الاختبار
     * المسار: POST /api/v1/quizzes/{quizId}/start
     */
    @PostMapping("/{quizId}/start")
    @PreAuthorize("isAuthenticated() and hasRole('STUDENT')")
    public ResponseEntity<?> startAttempt(
            @PathVariable UUID quizId,
            @AuthenticationPrincipal User student,
            @RequestBody DeviceCheckRequest deviceCheckRequest // <--- [جديد]
    ) {
        try {
            AttemptResponse response = quizService.startNewAttempt(quizId, student, deviceCheckRequest); // <--- تمرير
                                                                                                         // الـ Request
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [STUDENT] تسليم الاختبار بالكامل ومعالجة التصحيح الآلي والمراقبة
     * المسار: POST /api/v1/quizzes/submit
     */
    @PostMapping("/submit")
    @PreAuthorize("isAuthenticated() and hasRole('STUDENT')")
    public ResponseEntity<?> submitAttempt(
            @RequestBody QuizSubmissionRequest submission,
            @AuthenticationPrincipal User student) {
        try {
            SubmissionResponse response = quizService.submitQuizAttempt(student, submission);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}