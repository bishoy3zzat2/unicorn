package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.AdvantageFeatureRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class AdvantageFeatureService {
    private final AdvantageFeatureRepository featureRepository;
    public AdvantageFeatureService(AdvantageFeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }
    @Transactional
    public AdvantageFeature createFeature(AdvantageFeatureRequest request) {
        AdvantageFeature feature = new AdvantageFeature();
        feature.setTitle(request.title());
        feature.setDescription(request.description());
        feature.setIconUrl(request.iconUrl());
        feature.setOrderIndex(request.orderIndex());
        return featureRepository.save(feature);
    }
    @Transactional
    public AdvantageFeature updateFeature(Long id, AdvantageFeatureRequest request) {
        AdvantageFeature feature = featureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Feature not found."));
        feature.setTitle(request.title());
        feature.setDescription(request.description());
        feature.setIconUrl(request.iconUrl());
        feature.setOrderIndex(request.orderIndex());
        return featureRepository.save(feature);
    }
}