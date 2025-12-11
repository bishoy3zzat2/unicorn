package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course> {
    Optional<Course> findBySlug(String slug);
    Boolean existsBySlug(String slug);
    Optional<Course> findByTitleIgnoreCase(String title);
    List<Course> findByStatus(String status);
    List<Course> findByIsFeaturedTrueAndStatus(String status);
    long countByStatus(String status);
    Page<Course> findByStatus(String status, Pageable pageable);
    @Query("SELECT DISTINCT c FROM Course c " +
           "LEFT JOIN FETCH c.enrollments " +
           "LEFT JOIN FETCH c.reviews " +
           "LEFT JOIN FETCH c.courseBadges cb " +
           "LEFT JOIN FETCH cb.badge " +
           "WHERE c.status = :status")
    List<Course> findByStatusWithRelations(@Param("status") String status);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_instructors WHERE course_id = :courseId", nativeQuery = true)
    void deleteInstructorsLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_moderators WHERE course_id = :courseId", nativeQuery = true)
    void deleteModeratorsLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_allowed_users WHERE course_id = :courseId", nativeQuery = true)
    void deleteAllowedUsersLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_tags WHERE course_id = :courseId", nativeQuery = true)
    void deleteTagsLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_categories WHERE course_id = :courseId", nativeQuery = true)
    void deleteCategoriesLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_skills WHERE course_id = :courseId", nativeQuery = true)
    void deleteSkillsLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM bundle_courses WHERE course_id = :courseId", nativeQuery = true)
    void deleteBundleLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_providers_relation WHERE course_id = :courseId", nativeQuery = true)
    void deleteProvidersLinks(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM courses WHERE id = :courseId", nativeQuery = true)
    void deleteCourseById(@Param("courseId") UUID courseId);
    @Query("SELECT DISTINCT c FROM Course c " +
           "JOIN c.providers p " +
           "WHERE p.id = :providerId")
    List<Course> findByProviderId(@Param("providerId") UUID providerId);
}