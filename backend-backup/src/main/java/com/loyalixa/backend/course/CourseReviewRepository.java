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
public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    List<CourseReview> findByIsFeaturedTrue();
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_reviews WHERE course_id = :courseId", nativeQuery = true)
    void deleteByCourseId(@Param("courseId") UUID courseId);
}