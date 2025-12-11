package com.loyalixa.backend.user;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
public class FollowService {
    private final InstructorFollowerRepository followerRepository;
    private final UserRepository userRepository;
    public FollowService(InstructorFollowerRepository followerRepository, UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public boolean followInstructor(UUID followerId, UUID instructorId) {
        if (followerId.equals(instructorId)) {
            throw new IllegalStateException("You cannot follow yourself.");
        }
        User instructor = userRepository.findById(instructorId)
                .orElseThrow(() -> new IllegalArgumentException("Instructor not found."));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower (student) not found."));
        if (followerRepository.existsByInstructorIdAndFollowerId(instructorId, followerId)) {
            return true;  
        }
        InstructorFollower followEntry = new InstructorFollower();
        followEntry.setInstructor(instructor);
        followEntry.setFollower(follower);
        followerRepository.save(followEntry);
        return true;
    }
    @Transactional
    public boolean unfollowInstructor(UUID followerId, UUID instructorId) {
        if (!followerRepository.existsByInstructorIdAndFollowerId(instructorId, followerId)) {
            return false;  
        }
        followerRepository.delete(followerRepository.findByInstructorIdAndFollowerId(instructorId, followerId).orElseThrow());
        return true;
    }
    @Transactional(readOnly = true)
    public boolean isFollowing(UUID followerId, UUID instructorId) {
        return followerRepository.existsByInstructorIdAndFollowerId(instructorId, followerId);
    }
}