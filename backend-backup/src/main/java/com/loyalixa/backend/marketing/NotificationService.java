package com.loyalixa.backend.marketing;
import com.loyalixa.backend.course.Course;
import com.loyalixa.backend.user.InstructorFollower;
import com.loyalixa.backend.user.InstructorFollowerRepository;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class NotificationService {
    private final InstructorFollowerRepository followerRepository;
    public NotificationService(InstructorFollowerRepository followerRepository) {
        this.followerRepository = followerRepository;
    }
    public void notifyFollowersOfNewCourse(UUID instructorId, Course newCourse) {
        List<User> followers = followerRepository.findByInstructorId(instructorId).stream()
            .map(InstructorFollower::getFollower)
            .collect(Collectors.toList());
        System.out.println("--- NOTIFICATION TRIGGERED ---");
        System.out.println("New Course '" + newCourse.getTitle() + "' posted by Instructor: " + instructorId);
        System.out.println("Notifying " + followers.size() + " followers via EMAIL/APP...");
        System.out.println("------------------------------");
    }
}