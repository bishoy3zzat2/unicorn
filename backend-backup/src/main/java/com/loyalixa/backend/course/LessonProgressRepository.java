package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    List<LessonProgress> findByStudentId(UUID studentId);
    @Query("SELECT lp FROM LessonProgress lp JOIN FETCH lp.lesson l JOIN FETCH l.section s JOIN FETCH s.course WHERE lp.student.id = :studentId")
    List<LessonProgress> findByStudentIdWithCourse(UUID studentId);
    @Query("SELECT COUNT(lp) FROM LessonProgress lp WHERE lp.student.id = :studentId AND lp.isCompleted = true")
    Long countCompletedLessonsByStudentId(@Param("studentId") UUID studentId);
    @Query("SELECT SUM(lp.timeSpentSeconds) FROM LessonProgress lp WHERE lp.student.id = :studentId")
    Long sumTimeSpentByStudentId(@Param("studentId") UUID studentId);
}
