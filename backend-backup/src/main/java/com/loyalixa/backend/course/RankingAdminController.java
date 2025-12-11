package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.BadgeRankingWeightResponse;
import com.loyalixa.backend.course.dto.BadgeRankingWeightUpdateRequest;
import com.loyalixa.backend.course.dto.RankingWeightResponse;
import com.loyalixa.backend.course.dto.RankingWeightUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/admin/ranking")
public class RankingAdminController {
    private final RankingWeightsService rankingWeightsService;
    private final CourseRankingService courseRankingService;
    private final BadgeRankingWeightService badgeRankingWeightService;
    public RankingAdminController(
            RankingWeightsService rankingWeightsService,
            CourseRankingService courseRankingService,
            BadgeRankingWeightService badgeRankingWeightService) {
        this.rankingWeightsService = rankingWeightsService;
        this.courseRankingService = courseRankingService;
        this.badgeRankingWeightService = badgeRankingWeightService;
    }
    @GetMapping("/weights")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ranking:view')")
    public ResponseEntity<List<RankingWeightResponse>> getRankingWeights() {
        List<RankingWeightResponse> weights = rankingWeightsService.getAllWeights();
        return ResponseEntity.ok(weights);
    }
    @PutMapping("/weights")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ranking:update')")
    public ResponseEntity<List<RankingWeightResponse>> updateRankingWeights(
            @Valid @RequestBody RankingWeightUpdateRequest request) {
        try {
            List<RankingWeightResponse> updatedWeights = rankingWeightsService.updateWeights(request);
            return ResponseEntity.ok(updatedWeights);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/badge-weights")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ranking:badge:view')")
    public ResponseEntity<List<BadgeRankingWeightResponse>> getBadgeWeights() {
        List<BadgeRankingWeightResponse> weights = badgeRankingWeightService.getAllBadgeWeights();
        return ResponseEntity.ok(weights);
    }
    @PutMapping("/badge-weights")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ranking:badge:update')")
    public ResponseEntity<List<BadgeRankingWeightResponse>> updateBadgeWeights(
            @Valid @RequestBody BadgeRankingWeightUpdateRequest request) {
        try {
            List<BadgeRankingWeightResponse> updatedWeights = badgeRankingWeightService.updateBadgeWeights(request);
            return ResponseEntity.ok(updatedWeights);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
