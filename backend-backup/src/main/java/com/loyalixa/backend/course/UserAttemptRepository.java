package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface UserAttemptRepository extends JpaRepository<UserAttempt, UUID> {
    long countByQuizAndStatusIn(Quiz quiz, List<AttemptStatus> statuses);
}