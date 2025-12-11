package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CourseAdminResponse;
import com.loyalixa.backend.course.dto.CourseProviderRequest;
import com.loyalixa.backend.course.dto.CourseProviderResponse;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/admin/providers")
public class CourseProviderAdminController {
    private final CourseProviderService courseProviderService;
    private final CourseRepository courseRepository;
    private final CourseAdminService courseAdminService;
    public CourseProviderAdminController(CourseProviderService courseProviderService,
                                        CourseRepository courseRepository,
                                        CourseAdminService courseAdminService) {
        this.courseProviderService = courseProviderService;
        this.courseRepository = courseRepository;
        this.courseAdminService = courseAdminService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:get_all')")
    public ResponseEntity<List<CourseProviderResponse>> getAllProviders() {
        List<CourseProviderResponse> providers = courseProviderService.getAllProvidersResponse();
        return ResponseEntity.ok(providers);
    }
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('INSTRUCTOR') or hasAuthority('provider:get_all')")
    public ResponseEntity<List<CourseProviderResponse>> getActiveProviders() {
        List<CourseProviderResponse> providers = courseProviderService.getActiveProvidersResponse();
        return ResponseEntity.ok(providers);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:get_all')")
    public ResponseEntity<CourseProviderResponse> getProvider(@PathVariable UUID id) {
        try {
            CourseProviderResponse response = courseProviderService.getProviderResponse(id);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/slug/{slug}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:get_all')")
    public ResponseEntity<CourseProviderResponse> getProviderBySlug(@PathVariable String slug) {
        try {
            CourseProviderResponse response = courseProviderService.getProviderResponseBySlug(slug);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:create')")
    public ResponseEntity<CourseProviderResponse> createProvider(
            @Valid @RequestBody CourseProviderRequest request,
            @AuthenticationPrincipal User currentUser) {
        try {
            CourseProvider newProvider = courseProviderService.createProvider(request, currentUser);
            CourseProviderResponse response = courseProviderService.getProviderResponse(newProvider.getId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{providerId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:update')")
    public ResponseEntity<CourseProviderResponse> updateProvider(
            @PathVariable UUID providerId,
            @Valid @RequestBody CourseProviderRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            CourseProvider updatedProvider = courseProviderService.updateProvider(providerId, request, currentUser);
            CourseProviderResponse response = courseProviderService.getProviderResponse(updatedProvider.getId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @DeleteMapping("/{providerId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:delete')")
    public ResponseEntity<?> deleteProvider(@PathVariable UUID providerId) {
        try {
            courseProviderService.deleteProvider(providerId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                java.util.Map.of("error", e.getMessage())
            );
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                java.util.Map.of("error", e.getMessage())
            );
        }
    }
    @GetMapping("/{providerId}/courses")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('provider:get_all')")
    public ResponseEntity<List<CourseAdminResponse>> getProviderCourses(@PathVariable UUID providerId) {
        try {
            courseProviderService.getProviderResponse(providerId);
            List<Course> courses = courseRepository.findByProviderId(providerId);
            List<CourseAdminResponse> courseResponses = courses.stream()
                    .map(course -> courseAdminService.getCourseById(course.getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(courseResponses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
