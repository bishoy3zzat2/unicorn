package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.*;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/courses")
public class CourseAdminController {
    private final CourseAdminService courseAdminService;
    private final CourseService courseService;
    private final CategoryRepository categoryRepository;
    private final CourseSectionRepository courseSectionRepository;
    public CourseAdminController(CourseAdminService courseAdminService, CourseService courseService,
            CategoryRepository categoryRepository, CourseSectionRepository courseSectionRepository) {
        this.courseAdminService = courseAdminService;
        this.courseService = courseService;
        this.categoryRepository = categoryRepository;
        this.courseSectionRepository = courseSectionRepository;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasRole('MODERATOR') or hasAuthority('course:view')")
    public ResponseEntity<Page<CourseAdminResponse>> getAllCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String currentStage,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String instructor,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @AuthenticationPrincipal User currentUser) {
        Page<CourseAdminResponse> courses = courseAdminService.getAllCourses(page, size, status, approvalStatus,
                currentStage, search, createdBy, instructor, dateFrom, dateTo, currentUser);
        return ResponseEntity.ok(courses);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:view')")
    public ResponseEntity<CourseAdminResponse> getCourseById(@PathVariable UUID id) {
        CourseAdminResponse course = courseAdminService.getCourseById(id);
        return ResponseEntity.ok(course);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:create')")
    public ResponseEntity<?> createCourse(
            @Valid @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.createCourse(request, currentUser);
            return new ResponseEntity<>(course, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.updateCourse(id, request, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:approve')")
    public ResponseEntity<CourseAdminResponse> updateApprovalStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CourseApprovalRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.updateApprovalStatus(id, request, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PutMapping("/{id}/stage")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<CourseAdminResponse> updateStage(
            @PathVariable UUID id,
            @Valid @RequestBody CourseStageUpdateRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.updateStage(id, request, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @PutMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:archive')")
    public ResponseEntity<CourseAdminResponse> archiveCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.archiveCourse(id, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/{id}/unarchive")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:archive')")
    public ResponseEntity<CourseAdminResponse> unarchiveCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.unarchiveCourse(id, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:delete')")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            courseAdminService.deleteCourse(id, currentUser);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('course:view', 'discount:create', 'discount:update')")
    public ResponseEntity<List<com.loyalixa.backend.course.dto.CourseSearchResponse>> searchCoursesForDiscount(
            @RequestParam("q") String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(new java.util.ArrayList<>());
        }
        List<String> identifiers = java.util.Arrays.stream(query.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
        List<Course> courses = courseService.findCoursesByMultipleIdentifiers(identifiers);
        List<com.loyalixa.backend.course.dto.CourseSearchResponse> response = courses.stream()
                .map(course -> new com.loyalixa.backend.course.dto.CourseSearchResponse(
                        course.getId(),
                        course.getTitle(),
                        course.getSlug()))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('course:view', 'course:create', 'course:update')")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }
    @GetMapping("/{courseId}/sections")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('course:view', 'course:create', 'course:update')")
    public ResponseEntity<List<SectionResponse>> getCourseSections(@PathVariable UUID courseId) {
        try {
            List<SectionResponse> sections = courseAdminService.getCourseSections(courseId);
            return ResponseEntity.ok(sections);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/{courseId}/sections")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> createCourseSection(
            @PathVariable UUID courseId,
            @Valid @RequestBody SectionRequest request) {
        try {
            SectionResponse section = courseAdminService.createCourseSection(courseId, request);
            return new ResponseEntity<>(section, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{courseId}/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> updateCourseSection(
            @PathVariable UUID courseId,
            @PathVariable Long sectionId,
            @Valid @RequestBody SectionRequest request) {
        try {
            SectionResponse section = courseAdminService.updateCourseSection(courseId, sectionId, request);
            return ResponseEntity.ok(section);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{courseId}/sections/{sectionId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<Void> deleteCourseSection(
            @PathVariable UUID courseId,
            @PathVariable Long sectionId) {
        try {
            courseAdminService.deleteCourseSection(courseId, sectionId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/{id}/validate")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('course:view', 'course:update')")
    public ResponseEntity<com.loyalixa.backend.course.dto.CourseValidationResponse> validateCourse(
            @PathVariable UUID id) {
        try {
            com.loyalixa.backend.course.dto.CourseValidationResponse validation = courseAdminService.validateCourse(id);
            return ResponseEntity.ok(validation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/{id}/submit-for-review")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> submitForReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.submitForReview(id, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{id}/withdraw-from-review")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:update')")
    public ResponseEntity<?> withdrawFromReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.withdrawFromReview(id, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/{id}/start-review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> startReview(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.startReview(id, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}/admin-rating-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAdminRatingPoints(
            @PathVariable UUID id,
            @RequestBody com.loyalixa.backend.course.dto.AdminRatingPointsRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseAdminResponse course = courseAdminService.updateAdminRatingPoints(id, request, currentUser);
            return ResponseEntity.ok(course);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }
}