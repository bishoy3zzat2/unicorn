package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/quizzes")
public class QuizAdminController {
    private final QuizAdminService quizAdminService;
    public QuizAdminController(QuizAdminService quizAdminService) {
        this.quizAdminService = quizAdminService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:create')")
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        try {
            List<QuizResponse> quizzes = quizAdminService.getAllQuizzes();
            return ResponseEntity.ok(quizzes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:create')")
    public ResponseEntity<List<QuizResponse>> getQuizzesByCourse(@PathVariable UUID courseId) {
        try {
            List<QuizResponse> quizzes = quizAdminService.getQuizzesByCourse(courseId);
            return ResponseEntity.ok(quizzes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/by-lesson/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:create')")
    public ResponseEntity<List<QuizResponse>> getQuizzesByLesson(@PathVariable Long lessonId) {
        try {
            List<QuizResponse> quizzes = quizAdminService.getQuizzesByLesson(lessonId);
            return ResponseEntity.ok(quizzes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:create')")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable UUID quizId) {
        try {
            QuizResponse quiz = quizAdminService.getQuizById(quizId);
            return ResponseEntity.ok(quiz);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:create')")
    public ResponseEntity<?> createQuiz(@Valid @RequestBody QuizCreateRequest request) {
        try {
            QuizResponse quiz = quizAdminService.createQuiz(request);
            return new ResponseEntity<>(quiz, HttpStatus.CREATED);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:update')")
    public ResponseEntity<?> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody QuizUpdateRequest request
    ) {
        try {
            QuizResponse quiz = quizAdminService.updateQuiz(quizId, request);
            return ResponseEntity.ok(quiz);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('quiz:delete')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID quizId) {
        try {
            quizAdminService.deleteQuiz(quizId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
