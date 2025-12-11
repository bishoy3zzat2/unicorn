package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/lessons")
public class LessonAdminController {
    private final LessonAdminService lessonAdminService;
    public LessonAdminController(LessonAdminService lessonAdminService) {
        this.lessonAdminService = lessonAdminService;
    }
    @GetMapping("/by-section/{sectionId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:view')")
    public ResponseEntity<List<LessonResponse>> getLessonsBySection(@PathVariable Long sectionId) {
        try {
            List<LessonResponse> lessons = lessonAdminService.getLessonsBySection(sectionId);
            return ResponseEntity.ok(lessons);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> createLesson(@Valid @RequestBody LessonRequest request) {
        try {
            LessonResponse lesson = lessonAdminService.createLesson(request);
            return new ResponseEntity<>(lesson, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> updateLesson(
            @PathVariable Long lessonId,
            @Valid @RequestBody LessonUpdateRequest request
    ) {
        try {
            LessonResponse lesson = lessonAdminService.updateLesson(lessonId, request);
            return ResponseEntity.ok(lesson);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long lessonId) {
        try {
            lessonAdminService.deleteLesson(lessonId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> updateLessonsOrder(@Valid @RequestBody LessonOrderUpdateRequest request) {
        try {
            List<LessonResponse> lessons = lessonAdminService.updateLessonsOrder(request);
            return ResponseEntity.ok(lessons);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
