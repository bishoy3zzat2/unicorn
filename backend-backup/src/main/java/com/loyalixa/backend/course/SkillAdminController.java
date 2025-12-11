package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.SkillRequest;
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
@RequestMapping("/api/v1/admin/skills")
public class SkillAdminController {
    private final SkillService skillService;
    private final SkillRepository skillRepository;
    public SkillAdminController(SkillService skillService, SkillRepository skillRepository) {
        this.skillService = skillService;
        this.skillRepository = skillRepository;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('skill:get_all')")
    public ResponseEntity<List<com.loyalixa.backend.course.dto.SkillResponse>> getAllSkills(@AuthenticationPrincipal User currentUser) {
        List<com.loyalixa.backend.course.dto.SkillResponse> skills = skillService.getAllSkillsResponse();
        return ResponseEntity.ok(skills);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('skill:get_all')")
    public ResponseEntity<com.loyalixa.backend.course.dto.SkillResponse> getSkill(@PathVariable UUID id) {
        try {
            Skill skill = skillRepository.findByIdWithCreatedAndUpdatedBy(id)
                    .orElseThrow(() -> new IllegalArgumentException("Skill not found."));
            com.loyalixa.backend.course.dto.SkillResponse response = skillService.mapToSkillResponse(skill);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('skill:get_details')")
    public ResponseEntity<com.loyalixa.backend.course.dto.SkillDetailsResponse> getSkillDetails(@PathVariable UUID id) {
        try {
            com.loyalixa.backend.course.dto.SkillDetailsResponse details = skillService.getSkillDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('skill:create')")
    public ResponseEntity<com.loyalixa.backend.course.dto.SkillResponse> createSkill(@Valid @RequestBody SkillRequest request, @AuthenticationPrincipal User adminUser) {
        try {
            Skill newSkill = skillService.createSkill(request, adminUser);
            Skill loadedSkill = skillRepository.findByIdWithCreatedAndUpdatedBy(newSkill.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load created skill."));
            com.loyalixa.backend.course.dto.SkillResponse response = skillService.mapToSkillResponse(loadedSkill);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  
        }
    }
    @PutMapping("/{skillId}")
    @PreAuthorize("hasAuthority('skill:update')")
    public ResponseEntity<com.loyalixa.backend.course.dto.SkillResponse> updateSkill(
            @PathVariable UUID skillId,
            @Valid @RequestBody SkillRequest request,
            @AuthenticationPrincipal User adminUser
    ) {
        try {
            Skill updatedSkill = skillService.updateSkill(skillId, request, adminUser);
            Skill loadedSkill = skillRepository.findByIdWithCreatedAndUpdatedBy(updatedSkill.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load updated skill."));
            com.loyalixa.backend.course.dto.SkillResponse response = skillService.mapToSkillResponse(loadedSkill);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();  
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();  
        }
    }
    @DeleteMapping("/{skillId}")
    @PreAuthorize("hasAuthority('skill:delete')")
    public ResponseEntity<?> deleteSkill(@PathVariable UUID skillId) {
        try {
            skillService.deleteSkill(skillId);
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