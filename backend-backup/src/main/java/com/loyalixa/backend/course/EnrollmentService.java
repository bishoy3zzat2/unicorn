package com.loyalixa.backend.course;
import com.loyalixa.backend.subscription.SubscriptionService;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final SubscriptionService subscriptionService;
    public EnrollmentService(EnrollmentRepository enrollmentRepository, 
                            CourseRepository courseRepository,
                            SubscriptionService subscriptionService) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.subscriptionService = subscriptionService;
    }
    @Transactional
    public Enrollment enrollUserInCourse(User student, Course course, EnrollmentSource source) {
        Optional<Enrollment> existingEnrollment = enrollmentRepository.findByStudentIdAndCourseId(student.getId(), course.getId());
        if (existingEnrollment.isPresent()) {
            Enrollment enrollment = existingEnrollment.get();
            if (enrollment.getEnrollmentStatus().equals("ACTIVE")) {
                throw new IllegalStateException("Student is already active in this course.");
            }
        }
        boolean isBundleEnrollment = course.getBundles() != null && !course.getBundles().isEmpty();
        if (!subscriptionService.canEnrollInCourse(student.getId(), isBundleEnrollment)) {
            if (isBundleEnrollment) {
                throw new IllegalStateException("You have reached the maximum number of bundle enrollments allowed by your subscription plan.");
            } else {
                throw new IllegalStateException("You have reached the maximum number of course enrollments allowed by your subscription plan.");
            }
        }
        Enrollment newEnrollment = new Enrollment();
        newEnrollment.setStudent(student);
        newEnrollment.setCourse(course);
        newEnrollment.setEnrollmentDate(LocalDateTime.now());
        newEnrollment.setEnrollmentStatus("ACTIVE");
        newEnrollment.setSource(source); 
        newEnrollment.setStartDate(LocalDateTime.now()); 
        return enrollmentRepository.save(newEnrollment);
    }
}