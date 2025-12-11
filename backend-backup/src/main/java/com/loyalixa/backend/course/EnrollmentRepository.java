package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    List<Enrollment> findByStudentId(UUID studentId);
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.course WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentIdWithCourse(@Param("studentId") UUID studentId);
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    long countByCourseId(@Param("courseId") UUID courseId);
    @Query("SELECT e FROM Enrollment e WHERE e.course.id = :courseId")
    List<Enrollment> findByCourseId(@Param("courseId") UUID courseId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM enrollments WHERE course_id = :courseId", nativeQuery = true)
    void deleteByCourseId(@Param("courseId") UUID courseId);
}