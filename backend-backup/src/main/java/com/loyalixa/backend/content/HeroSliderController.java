package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.HeroSliderRequest;
import com.loyalixa.backend.content.dto.HeroSliderResponse;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/hero-sliders")
public class HeroSliderController {
    private final HeroSliderService heroSliderService;
    private final HeroSliderRepository heroSliderRepository;
    public HeroSliderController(HeroSliderService heroSliderService, HeroSliderRepository heroSliderRepository) {
        this.heroSliderService = heroSliderService;
        this.heroSliderRepository = heroSliderRepository;
    }
    @GetMapping
    public ResponseEntity<List<HeroSliderResponse>> getAllHeroSliders() {
        List<HeroSliderResponse> heroSliders = heroSliderService.getAllHeroSlidersResponse();
        return ResponseEntity.ok(heroSliders);
    }
    @GetMapping("/{id}")
    public ResponseEntity<HeroSliderResponse> getHeroSlider(@PathVariable Long id) {
        try {
            HeroSlider heroSlider = heroSliderRepository.findByIdWithCreatedAndUpdatedBy(id)
                    .orElseThrow(() -> new IllegalArgumentException("Hero Slider not found."));
            HeroSliderResponse response = heroSliderService.mapHeroSliderToResponse(heroSlider);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('hero_slider:create')")
    public ResponseEntity<HeroSliderResponse> createHeroSlider(@Valid @RequestBody HeroSliderRequest request,
            @AuthenticationPrincipal User adminUser) {
        try {
            HeroSlider newHeroSlider = heroSliderService.createHeroSlider(request, adminUser);
            HeroSlider loadedHeroSlider = heroSliderRepository.findByIdWithCreatedAndUpdatedBy(newHeroSlider.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load created Hero Slider."));
            HeroSliderResponse response = heroSliderService.mapHeroSliderToResponse(loadedHeroSlider);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('hero_slider:update')")
    public ResponseEntity<HeroSliderResponse> updateHeroSlider(
            @PathVariable Long id,
            @Valid @RequestBody HeroSliderRequest request,
            @AuthenticationPrincipal User adminUser) {
        try {
            HeroSlider updatedHeroSlider = heroSliderService.updateHeroSlider(id, request, adminUser);
            HeroSlider loadedHeroSlider = heroSliderRepository
                    .findByIdWithCreatedAndUpdatedBy(updatedHeroSlider.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load updated Hero Slider."));
            HeroSliderResponse response = heroSliderService.mapHeroSliderToResponse(loadedHeroSlider);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('hero_slider:delete')")
    public ResponseEntity<Void> deleteHeroSlider(@PathVariable Long id) {
        try {
            heroSliderService.deleteHeroSlider(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
