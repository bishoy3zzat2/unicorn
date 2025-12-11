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
public interface CourseSectionRepository extends JpaRepository<CourseSection, Long> {
    List<CourseSection> findByCourseIdOrderByOrderIndexAsc(UUID courseId);
    long countByCourseId(UUID courseId);
    boolean existsByCourseIdAndId(UUID courseId, Long sectionId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_sections WHERE course_id = :courseId", nativeQuery = true)
    int deleteByCourseId(@Param("courseId") UUID courseId);
}
