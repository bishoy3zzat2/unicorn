package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.TagRequest;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/tags")
public class TagAdminController {
    private final TagService tagService;
    private final TagRepository tagRepository;
    public TagAdminController(TagService tagService, TagRepository tagRepository) {
        this.tagService = tagService;
        this.tagRepository = tagRepository;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('tag:get_all')")
    public ResponseEntity<List<com.loyalixa.backend.course.dto.TagResponse>> getAllTags(@AuthenticationPrincipal User currentUser) {
        List<com.loyalixa.backend.course.dto.TagResponse> tags = tagService.getAllTagsResponse();
        return ResponseEntity.ok(tags);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tag:get_all')")
    public ResponseEntity<com.loyalixa.backend.course.dto.TagResponse> getTag(@PathVariable UUID id) {
        try {
            Tag tag = tagRepository.findByIdWithCreatedAndUpdatedBy(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tag not found."));
            com.loyalixa.backend.course.dto.TagResponse response = tagService.mapToTagResponse(tag);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('tag:get_details')")
    public ResponseEntity<com.loyalixa.backend.course.dto.TagDetailsResponse> getTagDetails(@PathVariable UUID id) {
        try {
            com.loyalixa.backend.course.dto.TagDetailsResponse details = tagService.getTagDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('tag:create')")
    public ResponseEntity<com.loyalixa.backend.course.dto.TagResponse> createTag(@Valid @RequestBody TagRequest request, @AuthenticationPrincipal User adminUser) {
        try {
            Tag newTag = tagService.createTag(request, adminUser);
            Tag loadedTag = tagRepository.findByIdWithCreatedAndUpdatedBy(newTag.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load created tag."));
            com.loyalixa.backend.course.dto.TagResponse response = tagService.mapToTagResponse(loadedTag);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }
    }
    @PutMapping("/{tagId}")
    @PreAuthorize("hasAuthority('tag:update')")
    public ResponseEntity<com.loyalixa.backend.course.dto.TagResponse> updateTag(
            @PathVariable UUID tagId,
            @Valid @RequestBody TagRequest request,
            @AuthenticationPrincipal User adminUser
    ) {
        try {
            Tag updatedTag = tagService.updateTag(tagId, request, adminUser);
            Tag loadedTag = tagRepository.findByIdWithCreatedAndUpdatedBy(updatedTag.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load updated tag."));
            com.loyalixa.backend.course.dto.TagResponse response = tagService.mapToTagResponse(loadedTag);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }
    }
    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasAuthority('tag:delete')")
    public ResponseEntity<?> deleteTag(@PathVariable UUID tagId) {
        try {
            tagService.deleteTag(tagId);
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
}