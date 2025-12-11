package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.BadgeRankingWeightResponse;
import com.loyalixa.backend.course.dto.BadgeRankingWeightUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class BadgeRankingWeightService {
    private final BadgeRankingWeightRepository badgeRankingWeightRepository;
    private final BadgeRepository badgeRepository;
    public BadgeRankingWeightService(
            BadgeRankingWeightRepository badgeRankingWeightRepository,
            BadgeRepository badgeRepository) {
        this.badgeRankingWeightRepository = badgeRankingWeightRepository;
        this.badgeRepository = badgeRepository;
    }
    @Transactional(readOnly = true)
    public List<BadgeRankingWeightResponse> getAllBadgeWeights() {
        List<BadgeRankingWeight> weights = badgeRankingWeightRepository.findAllWithBadges();
        List<Badge> allBadges = badgeRepository.findAll();
        List<UUID> badgesWithWeights = weights.stream()
                .map(w -> w.getBadge().getId())
                .collect(Collectors.toList());
        List<Badge> badgesWithoutWeights = allBadges.stream()
                .filter(b -> !badgesWithWeights.contains(b.getId()))
                .collect(Collectors.toList());
        List<BadgeRankingWeightResponse> result = weights.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        badgesWithoutWeights.forEach(badge -> {
            result.add(new BadgeRankingWeightResponse(
                    null,
                    badge.getId(),
                    badge.getName(),
                    badge.getIconClass(),
                    badge.getColorCode(),
                    null,  
                    null,
                    null
            ));
        });
        return result.stream()
                .sorted((a, b) -> {
                    String nameA = a.badgeName() != null ? a.badgeName() : "";
                    String nameB = b.badgeName() != null ? b.badgeName() : "";
                    return nameA.compareToIgnoreCase(nameB);
                })
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public Double getBadgeWeight(UUID badgeId) {
        return badgeRankingWeightRepository.findByBadgeId(badgeId)
                .map(BadgeRankingWeight::getWeightValue)
                .orElse(null);
    }
    @Transactional
    public List<BadgeRankingWeightResponse> updateBadgeWeights(BadgeRankingWeightUpdateRequest request) {
        for (BadgeRankingWeightUpdateRequest.BadgeWeightUpdateItem item : request.badgeWeights()) {
            Badge badge = badgeRepository.findById(item.badgeId())
                    .orElseThrow(() -> new IllegalArgumentException("Badge not found: " + item.badgeId()));
            Optional<BadgeRankingWeight> existing = badgeRankingWeightRepository.findByBadgeId(item.badgeId());
            if (item.weightValue() == null || item.weightValue() == 0) {
                existing.ifPresent(badgeRankingWeightRepository::delete);
            } else {
                BadgeRankingWeight weight = existing.orElse(new BadgeRankingWeight());
                weight.setBadge(badge);
                weight.setWeightValue(item.weightValue());
                badgeRankingWeightRepository.save(weight);
            }
        }
        return getAllBadgeWeights();
    }
    private BadgeRankingWeightResponse toResponse(BadgeRankingWeight weight) {
        Badge badge = weight.getBadge();
        return new BadgeRankingWeightResponse(
                weight.getId(),
                badge.getId(),
                badge.getName(),
                badge.getIconClass(),
                badge.getColorCode(),
                weight.getWeightValue(),
                weight.getCreatedAt(),
                weight.getUpdatedAt()
        );
    }
}
