package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface BadgeRankingWeightRepository extends JpaRepository<BadgeRankingWeight, Long> {
    Optional<BadgeRankingWeight> findByBadgeId(UUID badgeId);
    @Query("SELECT brw FROM BadgeRankingWeight brw " +
           "LEFT JOIN FETCH brw.badge " +
           "ORDER BY brw.badge.name ASC")
    List<BadgeRankingWeight> findAllWithBadges();
    boolean existsByBadgeId(UUID badgeId);
}
