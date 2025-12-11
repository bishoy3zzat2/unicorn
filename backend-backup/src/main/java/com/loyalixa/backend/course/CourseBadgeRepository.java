package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
@Repository
public interface CourseBadgeRepository extends JpaRepository<CourseBadge, CourseBadge.CourseBadgeId> {
    List<CourseBadge> findByCourseId(UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_badges WHERE course_id = :courseId", nativeQuery = true)
    void deleteByCourseId(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_badges WHERE expiration_date IS NOT NULL AND expiration_date < :now", nativeQuery = true)
    int deleteExpiredBadges(@Param("now") java.time.LocalDateTime now);
}
