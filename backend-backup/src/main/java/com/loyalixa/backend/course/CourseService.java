package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CourseCreateRequest;
import com.loyalixa.backend.marketing.NotificationService;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final SkillRepository skillRepository;
    private final NotificationService notificationService;
    private final CourseProviderRepository courseProviderRepository;
    public CourseService(TagRepository tagRepository, SkillRepository skillRepository,
            CourseRepository courseRepository, CategoryRepository categoryRepository, UserRepository userRepository,
            NotificationService notificationService, CourseProviderRepository courseProviderRepository) {
        this.courseRepository = courseRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.skillRepository = skillRepository;
        this.notificationService = notificationService;
        this.courseProviderRepository = courseProviderRepository;
    }
    @Transactional
    @SuppressWarnings("deprecation")
    public Course createNewCourse(CourseCreateRequest request, User createdBy) {
        if (courseRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Course with slug '" + request.slug() + "' already exists.");
        }
        Set<User> instructors = new HashSet<>();
        if (request.instructorIds() != null && !request.instructorIds().isEmpty()) {
            List<User> foundInstructors = userRepository.findAllById(request.instructorIds());
            if (foundInstructors.size() != request.instructorIds().size()) {
                throw new IllegalArgumentException("One or more instructor IDs were not found.");
            }
            for (User instructor : foundInstructors) {
                String roleName = instructor.getRole() != null ? instructor.getRole().getName() : null;
                if (roleName == null
                        || (!"INSTRUCTOR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName))) {
                    throw new IllegalArgumentException("User '" + instructor.getEmail() +
                            "' cannot be an instructor. User must have INSTRUCTOR or ADMIN role.");
                }
            }
            instructors.addAll(foundInstructors);
        } else {
            throw new IllegalArgumentException("Course must have at least one instructor.");
        }
        Set<Category> categories = new HashSet<>();
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<Category> foundCategories = categoryRepository.findAllById(request.categoryIds());
            if (foundCategories.size() != request.categoryIds().size()) {
                throw new IllegalArgumentException("One or more category IDs were not found.");
            }
            categories.addAll(foundCategories);
        }
        Set<Tag> tags = new HashSet<>();
        if (request.tags() != null && !request.tags().isEmpty()) {
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
        }
        Set<Skill> skills = new HashSet<>();
        if (request.skills() != null && !request.skills().isEmpty()) {
            for (String skillName : request.skills()) {
                if (skillName == null || skillName.trim().isEmpty())
                    continue;  
                Skill skill = skillRepository.findByNameIgnoreCase(skillName.trim())
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(skillName.trim());
                            return skillRepository.save(newSkill);
                        });
                skills.add(skill);
            }
        }
        Course newCourse = new Course();
        newCourse.setTitle(request.title());
        newCourse.setSlug(request.slug());
        newCourse.setPrice(request.price());
        newCourse.setLevel(request.level());
        newCourse.setInstructors(instructors);
        newCourse.setCategories(categories);
        newCourse.setTags(tags);
        newCourse.setSkills(skills);
        newCourse.setHasFreeContent(request.hasFreeContent());
        newCourse.setStatus("DRAFT");  
        newCourse.setCurrentStage("CORE_METADATA");  
        newCourse.setIsFeatured(false);
        newCourse.setShortDescription(request.shortDescription());
        newCourse.setFullDescription(request.fullDescription());
        newCourse.setDurationText(request.durationText());
        if (request.discountType() != null && request.discountValue() != null) {
            newCourse.setDiscountType(request.discountType());
            newCourse.setDiscountValue(request.discountValue());
            newCourse.setDiscountIsFixed(request.discountIsFixed() != null ? request.discountIsFixed() : true);
            newCourse.setDiscountDecayRate(request.discountDecayRate());
            newCourse.calculateDiscountPrice();  
        } else if (request.discountPrice() != null) {
            newCourse.setDiscountPrice(request.discountPrice());
            newCourse.setDiscountType(null);
            newCourse.setDiscountValue(null);
            newCourse.setDiscountIsFixed(true);
            newCourse.setDiscountDecayRate(null);
        } else {
            newCourse.setDiscountPrice(null);
            newCourse.setDiscountType(null);
            newCourse.setDiscountValue(null);
            newCourse.setDiscountIsFixed(true);
            newCourse.setDiscountDecayRate(null);
        }
        newCourse.setDiscountExpiresAt(request.discountExpiresAt());
        newCourse.setLearningFormat(request.learningFormat());
        newCourse.setLanguage(request.language());
        newCourse.setSubtitlesLanguages(request.subtitlesLanguages());
        newCourse.setAcademicDegree(request.academicDegree());
        if (request.providerIds() != null && !request.providerIds().isEmpty()) {
            Set<CourseProvider> providers = new HashSet<>(courseProviderRepository.findAllById(request.providerIds()));
            if (providers.size() != request.providerIds().size()) {
                throw new IllegalArgumentException("One or more course providers not found");
            }
            newCourse.setProviders(providers);
            if (!providers.isEmpty()) {
                CourseProvider firstProvider = providers.iterator().next();
                newCourse.setOrganizationName(firstProvider.getName());
                newCourse.setProviderLogoUrl(firstProvider.getLogoUrl());
            }
        } else {
            newCourse.setProviders(new HashSet<>());
            newCourse.setOrganizationName(request.organizationName());
            newCourse.setProviderLogoUrl(request.providerLogoUrl());
        }
        newCourse.setVisibility(request.visibility() != null ? request.visibility() : "PUBLIC");
        newCourse.setCreatedBy(createdBy);
        newCourse.setApprovalStatus(null);
        Course savedCourse = courseRepository.save(newCourse);
        for (User instructor : instructors) {
            notificationService.notifyFollowersOfNewCourse(
                    instructor.getId(),  
                    savedCourse);
        }
        return savedCourse;
    }
    @Transactional(readOnly = true)
    @Cacheable(value = "publishedCourses")
    public Page<Course> getPublishedCoursesPaged(Pageable pageable) {
        return courseRepository.findByStatus("PUBLISHED", pageable);
    }
    @Transactional(readOnly = true)
    public List<Course> findCoursesByMultipleIdentifiers(List<String> identifiers) {
        List<Course> results = new java.util.ArrayList<>();
        List<java.util.UUID> foundIds = new java.util.ArrayList<>();  
        for (String identifier : identifiers) {
            if (identifier == null || identifier.trim().isEmpty()) {
                continue;
            }
            String trimmed = identifier.trim();
            try {
                java.util.UUID courseId = java.util.UUID.fromString(trimmed);
                Course course = courseRepository.findById(courseId).orElse(null);
                if (course != null && !foundIds.contains(courseId)) {
                    results.add(course);
                    foundIds.add(courseId);
                }
                continue;  
            } catch (IllegalArgumentException e) {
            }
            Course courseBySlug = courseRepository.findBySlug(trimmed).orElse(null);
            if (courseBySlug != null && !foundIds.contains(courseBySlug.getId())) {
                results.add(courseBySlug);
                foundIds.add(courseBySlug.getId());
                continue;
            }
            Course courseByTitle = courseRepository.findByTitleIgnoreCase(trimmed).orElse(null);
            if (courseByTitle != null && !foundIds.contains(courseByTitle.getId())) {
                results.add(courseByTitle);
                foundIds.add(courseByTitle.getId());
            }
        }
        return results;
    }
    @Transactional(readOnly = true)
    public Course getCourseBySlugWithAccessCheck(String slug, User currentUser,
            EnrollmentRepository enrollmentRepository) {
        Course course = courseRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + slug));
        String status = course.getStatus();
        if ("PUBLISHED".equals(status)) {
            return course;
        }
        if ("DRAFT".equals(status)) {
            throw new IllegalStateException("Course is not available (DRAFT status)");
        }
        if ("ARCHIVED".equals(status)) {
            if (currentUser == null) {
                throw new IllegalStateException("This course is archived. Only enrolled students can access it.");
            }
            boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
            if (isAdmin) {
                return course;
            }
            boolean isEnrolled = enrollmentRepository.findByStudentIdAndCourseId(currentUser.getId(), course.getId())
                    .isPresent();
            if (!isEnrolled) {
                throw new IllegalStateException("This course is archived. Only enrolled students can access it.");
            }
            return course;
        }
        throw new IllegalStateException("Course is not available");
    }
}