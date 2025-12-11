package com.loyalixa.backend.home;
import com.loyalixa.backend.content.*; 
import com.loyalixa.backend.content.dto.AdvantageFeatureResponse;
import com.loyalixa.backend.content.dto.FaqResponse;
import com.loyalixa.backend.content.dto.HomeResponse;
import com.loyalixa.backend.content.dto.PartnerResponse;
import com.loyalixa.backend.course.*;
import com.loyalixa.backend.course.dto.*;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.dto.UserPublicResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
@Service
public class HomeService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseReviewRepository courseReviewRepository; 
    private final FaqRepository faqRepository;
    private final PartnerRepository partnerRepository;
    private final AdvantageFeatureRepository advantageFeatureRepository;
    public HomeService(UserRepository userRepository, CourseRepository courseRepository, CourseReviewRepository courseReviewRepository, FaqRepository faqRepository, PartnerRepository partnerRepository, AdvantageFeatureRepository advantageFeatureRepository) {
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.faqRepository = faqRepository;
        this.partnerRepository = partnerRepository;
        this.advantageFeatureRepository = advantageFeatureRepository;
    }
    @Transactional(readOnly = true)
    public HomeResponse getHomePageData() {
        long totalStudents = userRepository.count(); 
        long totalCourses = courseRepository.countByStatus("PUBLISHED"); 
        long totalInstructors = userRepository.countByRoleName("INSTRUCTOR");
        List<CourseResponse> courseResponses = Collections.emptyList();
        List<ReviewResponse> reviewResponses = Collections.emptyList();
        List<AdvantageFeatureResponse> advantageResponses = advantageFeatureRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(f -> new AdvantageFeatureResponse(f.getTitle(), f.getDescription(), f.getIconUrl()))
            .collect(Collectors.toList());
        List<FaqResponse> faqResponses = faqRepository.findAll().stream() 
            .map(f -> new FaqResponse(
                f.getId(),
                f.getQuestion(),
                f.getAnswer(),
                f.getCategory(),
                f.getOrderIndex(),
                f.getCreatedAt(),
                f.getUpdatedAt(),
                null,  
                null   
            ))
            .collect(Collectors.toList());
        List<PartnerResponse> partnerResponses = partnerRepository.findAllByOrderByOrderIndexAsc().stream()
            .map(p -> new PartnerResponse(p.getName(), p.getLogoUrl()))
            .collect(Collectors.toList());
        return new HomeResponse(totalStudents, totalCourses,totalInstructors , courseResponses, reviewResponses, advantageResponses, faqResponses, partnerResponses);
    }
    private CourseResponse mapCourseToResponse(Course course) {
        List<UserPublicResponse> instructors = course.getInstructors().stream()
            .map(i -> new UserPublicResponse(i.getId(), i.getUsername(), i.getRole().getName()))
            .collect(Collectors.toList());
        return new CourseResponse(course.getId(), course.getTitle(), course.getSlug(), course.getPrice(), course.getDiscountPrice(), course.getLevel(), course.getDurationText(), course.getCoverImageUrl(), instructors);
    }
    private ReviewResponse mapReviewToResponse(CourseReview review) {
        User user = review.getUser();
        UserPublicResponse student = new UserPublicResponse(user.getId(), user.getUsername(), user.getRole().getName());
        return new ReviewResponse(review.getComment(), review.getRating(), student);
    }
}