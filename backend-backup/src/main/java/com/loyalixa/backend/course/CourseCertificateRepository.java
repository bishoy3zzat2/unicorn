package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CourseCertificateRepository extends JpaRepository<CourseCertificate, UUID> {
    Optional<CourseCertificate> findBySlug(String slug);
    Optional<CourseCertificate> findByCourseId(UUID courseId);
    boolean existsBySlug(String slug);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM course_certificates WHERE course_id = :courseId", nativeQuery = true)
    void deleteByCourseId(@Param("courseId") UUID courseId);
}
