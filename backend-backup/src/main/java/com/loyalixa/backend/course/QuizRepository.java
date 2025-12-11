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
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findByLessonId(Long lessonId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM quizzes WHERE lesson_id IN (SELECT id FROM lessons WHERE section_id = :sectionId)", nativeQuery = true)
    int deleteBySectionId(@Param("sectionId") Long sectionId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM quizzes WHERE lesson_id IN (SELECT l.id FROM lessons l JOIN course_sections cs ON l.section_id = cs.id WHERE cs.course_id = :courseId)", nativeQuery = true)
    int deleteByCourseId(@Param("courseId") UUID courseId);
}