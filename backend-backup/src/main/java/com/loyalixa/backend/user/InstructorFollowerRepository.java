package com.loyalixa.backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface InstructorFollowerRepository extends JpaRepository<InstructorFollower, InstructorFollower.InstructorFollowerId> {
    List<InstructorFollower> findByInstructorId(UUID instructorId);
    boolean existsByInstructorIdAndFollowerId(UUID instructorId, UUID followerId);
    Optional<InstructorFollower> findByInstructorIdAndFollowerId(UUID instructorId, UUID followerId);
    void deleteByInstructorIdAndFollowerId(UUID instructorId, UUID followerId);
}