package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.HeroSliderRequest;
import com.loyalixa.backend.content.dto.HeroSliderResponse;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class HeroSliderService {
    private final HeroSliderRepository heroSliderRepository;
    public HeroSliderService(HeroSliderRepository heroSliderRepository) {
        this.heroSliderRepository = heroSliderRepository;
    }
    @Transactional
    public HeroSlider createHeroSlider(HeroSliderRequest request, User adminUser) {
        HeroSlider heroSlider = new HeroSlider();
        heroSlider.setMainTitle(request.mainTitle());
        heroSlider.setDescription(request.description());
        heroSlider.setMediaUrl(request.mediaUrl());
        heroSlider.setMediaType(request.mediaType());
        heroSlider.setButtonText(request.buttonText());
        heroSlider.setButtonLink(request.buttonLink());
        heroSlider.setDisplayDurationMs(request.displayDurationMs() != null ? request.displayDurationMs() : 1000);
        heroSlider.setAutoplay(request.autoplay() != null ? Boolean.TRUE.equals(request.autoplay()) : false);
        heroSlider.setLoop(request.loop() != null ? Boolean.TRUE.equals(request.loop()) : false);
        heroSlider.setMuted(request.muted() != null ? Boolean.TRUE.equals(request.muted()) : false);
        heroSlider.setControls(request.controls() != null ? Boolean.TRUE.equals(request.controls()) : true);
        heroSlider.setOrderIndex(request.orderIndex());
        heroSlider.setCreatedBy(adminUser);
        return heroSliderRepository.save(heroSlider);
    }
    @Transactional
    public HeroSlider createHeroSlider(HeroSliderRequest request) {
        return createHeroSlider(request, null);
    }
    @Transactional(readOnly = true)
    public List<HeroSlider> getAllHeroSliders() {
        return heroSliderRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<HeroSlider> getAllHeroSlidersWithAuditing() {
        return heroSliderRepository.findAllWithCreatedAndUpdatedBy();
    }
    @Transactional(readOnly = true)
    public List<HeroSliderResponse> getAllHeroSlidersResponse() {
        List<HeroSlider> heroSliders = heroSliderRepository.findAllWithCreatedAndUpdatedBy();
        return heroSliders.stream()
                .map(this::mapToHeroSliderResponse)
                .collect(Collectors.toList());
    }
    private HeroSliderResponse mapToHeroSliderResponse(HeroSlider heroSlider) {
        HeroSliderResponse.UserInfo createdByInfo = null;
        if (heroSlider.getCreatedBy() != null) {
            var createdBy = heroSlider.getCreatedBy();
            createdByInfo = new HeroSliderResponse.UserInfo(
                    createdBy.getId(),
                    createdBy.getEmail(),
                    createdBy.getUsername());
        }
        HeroSliderResponse.UserInfo updatedByInfo = null;
        if (heroSlider.getUpdatedBy() != null) {
            var updatedBy = heroSlider.getUpdatedBy();
            updatedByInfo = new HeroSliderResponse.UserInfo(
                    updatedBy.getId(),
                    updatedBy.getEmail(),
                    updatedBy.getUsername());
        }
        return new HeroSliderResponse(
                heroSlider.getId(),
                heroSlider.getMainTitle(),
                heroSlider.getDescription(),
                heroSlider.getMediaUrl(),
                heroSlider.getMediaType(),
                heroSlider.getButtonText(),
                heroSlider.getButtonLink(),
                heroSlider.getDisplayDurationMs(),
                heroSlider.getAutoplay(),
                heroSlider.getLoop(),
                heroSlider.getMuted(),
                heroSlider.getControls(),
                heroSlider.getOrderIndex(),
                heroSlider.getCreatedAt(),
                heroSlider.getUpdatedAt(),
                createdByInfo,
                updatedByInfo);
    }
    @Transactional
    public HeroSlider updateHeroSlider(Long id, HeroSliderRequest request, User adminUser) {
        HeroSlider heroSlider = heroSliderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hero Slider not found"));
        heroSlider.setMainTitle(request.mainTitle());
        heroSlider.setDescription(request.description());
        heroSlider.setMediaUrl(request.mediaUrl());
        heroSlider.setMediaType(request.mediaType());
        heroSlider.setButtonText(request.buttonText());
        heroSlider.setButtonLink(request.buttonLink());
        heroSlider.setDisplayDurationMs(request.displayDurationMs() != null ? request.displayDurationMs() : 1000);
        heroSlider.setAutoplay(request.autoplay() != null ? Boolean.TRUE.equals(request.autoplay()) : false);
        heroSlider.setLoop(request.loop() != null ? Boolean.TRUE.equals(request.loop()) : false);
        heroSlider.setMuted(request.muted() != null ? Boolean.TRUE.equals(request.muted()) : false);
        heroSlider.setControls(request.controls() != null ? Boolean.TRUE.equals(request.controls()) : true);
        heroSlider.setOrderIndex(request.orderIndex());
        heroSlider.setUpdatedBy(adminUser);
        return heroSliderRepository.save(heroSlider);
    }
    @Transactional
    public HeroSlider updateHeroSlider(Long id, HeroSliderRequest request) {
        return updateHeroSlider(id, request, null);
    }
    @Transactional(readOnly = true)
    public HeroSliderResponse mapHeroSliderToResponse(HeroSlider heroSlider) {
        return mapToHeroSliderResponse(heroSlider);
    }
    @Transactional
    public void deleteHeroSlider(Long id) {
        if (!heroSliderRepository.existsById(id)) {
            throw new IllegalArgumentException("Hero Slider not found");
        }
        heroSliderRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public HeroSlider getHeroSliderById(Long id) {
        return heroSliderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Hero Slider not found."));
    }
}
