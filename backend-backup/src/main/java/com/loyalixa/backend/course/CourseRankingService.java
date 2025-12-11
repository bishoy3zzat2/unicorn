package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.RankedCourseResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class CourseRankingService {
    private final CourseRepository courseRepository;
    private final RankingWeightsService rankingWeightsService;
    public CourseRankingService(
            CourseRepository courseRepository,
            @Lazy RankingWeightsService rankingWeightsService) {
        this.courseRepository = courseRepository;
        this.rankingWeightsService = rankingWeightsService;
    }
    @Transactional(readOnly = true)
    public Page<RankedCourseResponse> getRankedCourses(Pageable pageable) {
        List<Course> allPublishedCourses = courseRepository.findByStatusWithRelations("PUBLISHED");
        List<RankedCourseResponse> rankedCourses = allPublishedCourses.stream()
                .map(this::calculateCourseScore)
                .sorted(Comparator.comparing(RankedCourseResponse::finalScore).reversed())  
                .collect(Collectors.toList());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), rankedCourses.size());
        List<RankedCourseResponse> pagedContent = rankedCourses.subList(start, end);
        return new PageImpl<>(pagedContent, pageable, rankedCourses.size());
    }
    private RankedCourseResponse calculateCourseScore(Course course) {
        double finalScore = 0.0;
        double reviewScore = calculateReviewScore(course);
        finalScore += reviewScore;
        double enrollmentScore = calculateEnrollmentScore(course);
        finalScore += enrollmentScore;
        double freshnessScore = calculateFreshnessScore(course);
        finalScore += freshnessScore;
        double badgeScore = calculateBadgeScore(course);
        finalScore += badgeScore;
        double adminRatingPoints = calculateAdminRatingPoints(course);
        finalScore += adminRatingPoints;
        int enrollmentCount = course.getEnrollments() != null ? course.getEnrollments().size() : 0;
        double averageRating = calculateAverageRating(course);
        List<String> badgeNames = course.getCourseBadges() != null
                ? course.getCourseBadges().stream()
                    .map(cb -> cb.getBadge() != null ? cb.getBadge().getName() : null)
                    .filter(name -> name != null)
                    .collect(Collectors.toList())
                : List.of();
        return new RankedCourseResponse(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getShortDescription(),
                course.getPrice(),
                course.getDiscountPrice(),
                course.getLevel(),
                course.getCoverImageUrl(),
                course.getStatus(),
                course.getApprovalStatus(),
                course.getIsFeatured(),
                course.getCreatedAt(),
                finalScore,
                enrollmentCount,
                averageRating,
                badgeNames
        );
    }
    private double calculateReviewScore(Course course) {
        try {
            RankingWeights weight = rankingWeightsService.getWeightByFactorName("REVIEW_MULTIPLIER");
            double averageRating = calculateAverageRating(course);
            return averageRating * weight.getWeightValue();
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }
    private double calculateAverageRating(Course course) {
        if (course.getReviews() == null || course.getReviews().isEmpty()) {
            return 0.0;
        }
        double sum = course.getReviews().stream()
                .filter(review -> {
                    String status = review.getStatus();
                    return "APPROVED".equals(status) || "PENDING".equals(status);
                })
                .mapToInt(CourseReview::getRating)
                .sum();
        long count = course.getReviews().stream()
                .filter(review -> {
                    String status = review.getStatus();
                    return "APPROVED".equals(status) || "PENDING".equals(status);
                })
                .count();
        return count > 0 ? sum / count : 0.0;
    }
    private double calculateEnrollmentScore(Course course) {
        try {
            RankingWeights weight = rankingWeightsService.getWeightByFactorName("ENROLLMENT_SCORE");
            int enrollmentCount = course.getEnrollments() != null ? course.getEnrollments().size() : 0;
            return enrollmentCount * weight.getWeightValue();
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }
    private double calculateFreshnessScore(Course course) {
        try {
            RankingWeights weight = rankingWeightsService.getWeightByFactorName("FRESHNESS_BOOST");
            if (course.getCreatedAt() == null) {
                return 0.0;
            }
            long daysSinceCreation = ChronoUnit.DAYS.between(course.getCreatedAt(), LocalDateTime.now());
            double decayRate = weight.getDecayRate() != null ? weight.getDecayRate() : 0.1;
            int decayPeriodDays = weight.getDecayPeriodDays() != null ? weight.getDecayPeriodDays() : 30;
            double period = decayPeriodDays > 0 ? decayPeriodDays : 1;
            double decayFactor = Math.max(0, 1 - (decayRate * daysSinceCreation / period));
            return weight.getWeightValue() * decayFactor;
        } catch (IllegalArgumentException e) {
            return 0.0;
        }
    }
    private double calculateBadgeScore(Course course) {
        if (course.getCourseBadges() == null || course.getCourseBadges().isEmpty()) {
            return 0.0;
        }
        double totalScore = 0.0;
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (CourseBadge courseBadge : course.getCourseBadges()) {
            java.time.LocalDateTime expirationDate = courseBadge.getExpirationDate();
            if (expirationDate != null && expirationDate.isBefore(now)) {
                continue;
            }
            Badge badge = courseBadge.getBadge();
            if (badge != null && badge.getWeight() != null && badge.getWeight() > 0) {
                totalScore += badge.getWeight();
            }
        }
        return totalScore;
    }
    private double calculateAdminRatingPoints(Course course) {
        if (course.getAdminRatingPoints() == null || course.getAdminRatingPoints() == 0) {
            return 0.0;
        }
        int points = course.getAdminRatingPoints();
        if (course.getRatingPointsIsFixed() != null && course.getRatingPointsIsFixed()) {
            return points;
        }
        if (course.getRatingPointsExpiresAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            if (course.getRatingPointsExpiresAt().isBefore(now)) {
                return 0.0;
            }
            return points;
        }
        return points;
    }
    public void evictRankedCoursesCache() {
    }
}
