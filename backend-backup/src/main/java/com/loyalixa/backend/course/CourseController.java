package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CourseCreateRequest;
import com.loyalixa.backend.course.dto.RankedCourseResponse;
import com.loyalixa.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;  
import org.springframework.data.web.PageableDefault;  
import java.util.List;
@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseRankingService courseRankingService;
    private final EnrollmentRepository enrollmentRepository;
    public CourseController(CourseService courseService, CourseRankingService courseRankingService, EnrollmentRepository enrollmentRepository) {
        this.courseService = courseService;
        this.courseRankingService = courseRankingService;
        this.enrollmentRepository = enrollmentRepository;
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:create')")
    public ResponseEntity<?> createCourse(
            @RequestBody CourseCreateRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            Course newCourse = courseService.createNewCourse(request, currentUser);
            return new ResponseEntity<>(newCourse, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping 
    public ResponseEntity<Page<Course>> getCourses(
        @PageableDefault(size = 20, sort = "title") Pageable pageable 
    ) {
        Page<Course> coursePage = courseService.getPublishedCoursesPaged(pageable);
        return ResponseEntity.ok(coursePage);
    }
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasAnyAuthority('course:view', 'discount:create', 'discount:update')")
    public ResponseEntity<List<com.loyalixa.backend.course.dto.CourseSearchResponse>> searchCoursesMultiple(
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
                        course.getSlug()
                ))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @GetMapping("/ranked")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('course:view')")
    public ResponseEntity<Page<RankedCourseResponse>> getRankedCourses(
            @PageableDefault(size = 20, sort = "finalScore") Pageable pageable) {
        Page<RankedCourseResponse> rankedCourses = courseRankingService.getRankedCourses(pageable);
        return ResponseEntity.ok(rankedCourses);
    }
    @GetMapping("/by-slug/{slug}")
    public ResponseEntity<?> getCourseBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal User currentUser) {
        try {
            Course course = courseService.getCourseBySlugWithAccessCheck(slug, currentUser, enrollmentRepository);
            return ResponseEntity.ok(course);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }
}