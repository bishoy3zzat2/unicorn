package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.RankingWeightResponse;
import com.loyalixa.backend.course.dto.RankingWeightUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class RankingWeightsService {
    private final RankingWeightsRepository rankingWeightsRepository;
    private CourseRankingService courseRankingService;
    public RankingWeightsService(RankingWeightsRepository rankingWeightsRepository) {
        this.rankingWeightsRepository = rankingWeightsRepository;
    }
    @PostConstruct
    @Transactional
    public void init() {
        initializeDefaultWeights();
    }
    @Autowired(required = false)
    public void setCourseRankingService(@Lazy CourseRankingService courseRankingService) {
        this.courseRankingService = courseRankingService;
    }
    @Transactional
    public void initializeDefaultWeights() {
        createIfNotExists("REVIEW_MULTIPLIER", 1000.0, null, null, 
            "مضاعف متوسط التقييم (كلما زاد التقييم، زادت النقاط)");
        createIfNotExists("ENROLLMENT_SCORE", 50.0, null, null, 
            "نقاط لكل اشتراك في الكورس (كلما زاد عدد المشتركين، زادت النقاط)");
        createIfNotExists("FRESHNESS_BOOST", 500.0, 0.1, 30, 
            "نقاط إضافية للكورسات الجديدة (تنخفض مع الوقت حسب decayRate و decayPeriodDays)");
        createIfNotExists("BADGE_BOOST", 300.0, null, null, 
            "نقاط إضافية لكل شارة مرتبطة بالكورس (لأغراض تسويقية)");
    }
    private void createIfNotExists(String factorName, Double weightValue, Double decayRate, Integer decayPeriodDays, String description) {
        if (!rankingWeightsRepository.existsByFactorName(factorName)) {
            RankingWeights weight = new RankingWeights();
            weight.setFactorName(factorName);
            weight.setWeightValue(weightValue);
            weight.setDecayRate(decayRate);
            weight.setDecayPeriodDays(decayPeriodDays);
            weight.setDescription(description);
            rankingWeightsRepository.save(weight);
        }
    }
    @Transactional(readOnly = true)
    public List<RankingWeightResponse> getAllWeights() {
        return rankingWeightsRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public List<RankingWeightResponse> updateWeights(RankingWeightUpdateRequest request) {
        for (RankingWeightUpdateRequest.WeightUpdateItem item : request.weights()) {
            RankingWeights weight = rankingWeightsRepository.findByFactorName(item.factorName())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Factor not found: " + item.factorName()));
            weight.setWeightValue(item.weightValue());
            if (item.decayRate() != null) {
                weight.setDecayRate(item.decayRate());
            }
            if (item.decayPeriodDays() != null) {
                weight.setDecayPeriodDays(item.decayPeriodDays());
            }
            rankingWeightsRepository.save(weight);
        }
        if (courseRankingService != null) {
            courseRankingService.evictRankedCoursesCache();
        }
        return getAllWeights();
    }
    @Transactional(readOnly = true)
    public RankingWeights getWeightByFactorName(String factorName) {
        return rankingWeightsRepository.findByFactorName(factorName)
                .orElseThrow(() -> new IllegalArgumentException("Factor not found: " + factorName));
    }
    private RankingWeightResponse toResponse(RankingWeights weight) {
        Integer daysUntilZero = null;
        if ("FRESHNESS_BOOST".equals(weight.getFactorName())) {
            Double dr = weight.getDecayRate();
            Integer period = weight.getDecayPeriodDays();
            if (dr != null && dr > 0 && period != null && period > 0) {
                daysUntilZero = (int) Math.ceil(period / dr);
            }
        }
        return new RankingWeightResponse(
                weight.getId(),
                weight.getFactorName(),
                weight.getWeightValue(),
                weight.getDecayRate(),
                weight.getDecayPeriodDays(),
                daysUntilZero,
                weight.getDescription(),
                weight.getCreatedAt(),
                weight.getUpdatedAt()
        );
    }
}
