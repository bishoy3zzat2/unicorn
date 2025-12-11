package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    long countBySectionId(Long sectionId);
    List<Lesson> findBySectionIdOrderByOrderIndexAsc(Long sectionId);
    List<Lesson> findBySectionId(Long sectionId);
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.section.course.id = :courseId")
    long countByCourseId(@Param("courseId") java.util.UUID courseId);
    @Query("SELECT COALESCE(MAX(l.orderIndex), 0) FROM Lesson l WHERE l.section.id = :sectionId")
    Integer findMaxOrderIndexBySectionId(@Param("sectionId") Long sectionId);
    boolean existsBySectionIdAndId(Long sectionId, Long lessonId);
    @Query("SELECT l FROM Lesson l JOIN FETCH l.section WHERE l.id = :lessonId")
    Optional<Lesson> findByIdWithSection(@Param("lessonId") Long lessonId);
    @Query("SELECT l FROM Lesson l JOIN FETCH l.section s WHERE s.course.id = :courseId")
    List<Lesson> findBySectionCourseId(@Param("courseId") java.util.UUID courseId);
    @Query("SELECT l FROM Lesson l WHERE l.quiz.id = :quizId")
    List<Lesson> findByQuizId(@Param("quizId") java.util.UUID quizId);
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.quiz = NULL WHERE l.quiz.id = :quizId")
    int unlinkQuizFromLessons(@Param("quizId") java.util.UUID quizId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lessons WHERE section_id = :sectionId", nativeQuery = true)
    int deleteBySectionId(@Param("sectionId") Long sectionId);
    @Modifying
    @Transactional
    @Query("UPDATE Lesson l SET l.quiz = NULL WHERE l.section.id = :sectionId AND l.quiz IS NOT NULL")
    int unlinkQuizzesFromLessonsBySection(@Param("sectionId") Long sectionId);
}