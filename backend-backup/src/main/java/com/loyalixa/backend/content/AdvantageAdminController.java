package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.AdvantageFeatureRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/advantages")
public class AdvantageAdminController {
    private final AdvantageFeatureService featureService;
    private final AdvantageFeatureRepository featureRepository;
    public AdvantageAdminController(AdvantageFeatureService featureService,
            AdvantageFeatureRepository featureRepository) {
        this.featureService = featureService;
        this.featureRepository = featureRepository;
    }
    @GetMapping
    public ResponseEntity<List<AdvantageFeature>> getAllFeatures() {
        return ResponseEntity.ok(featureRepository.findAllByOrderByOrderIndexAsc());
    }
    @PostMapping
    @PreAuthorize("hasAuthority('advantage:create')")
    public ResponseEntity<AdvantageFeature> createFeature(@Valid @RequestBody AdvantageFeatureRequest request) {
        AdvantageFeature newFeature = featureService.createFeature(request);
        return new ResponseEntity<>(newFeature, HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('advantage:update')")
    public ResponseEntity<AdvantageFeature> updateFeature(@PathVariable Long id,
            @Valid @RequestBody AdvantageFeatureRequest request) {
        AdvantageFeature updatedFeature = featureService.updateFeature(id, request);
        return ResponseEntity.ok(updatedFeature);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('advantage:delete')")
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        featureRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}