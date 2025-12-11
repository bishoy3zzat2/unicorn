package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class CourseAdminService {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final SkillRepository skillRepository;
    private final BadgeRepository badgeRepository;
    private final CourseBadgeRepository courseBadgeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final com.loyalixa.backend.discount.DiscountCodeRepository discountCodeRepository;
    private final CourseProviderRepository courseProviderRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final com.loyalixa.backend.marketing.GiftVoucherRepository giftVoucherRepository;
    private final CoursePrerequisiteRepository coursePrerequisiteRepository;
    private final CourseCertificateRepository courseCertificateRepository;
    public CourseAdminService(
            CourseRepository courseRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            SkillRepository skillRepository,
            BadgeRepository badgeRepository,
            CourseBadgeRepository courseBadgeRepository,
            EnrollmentRepository enrollmentRepository,
            com.loyalixa.backend.discount.DiscountCodeRepository discountCodeRepository,
            CourseProviderRepository courseProviderRepository,
            CourseSectionRepository courseSectionRepository,
            LessonRepository lessonRepository,
            QuizRepository quizRepository,
            CourseReviewRepository courseReviewRepository,
            com.loyalixa.backend.marketing.GiftVoucherRepository giftVoucherRepository,
            CoursePrerequisiteRepository coursePrerequisiteRepository,
            CourseCertificateRepository courseCertificateRepository) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.skillRepository = skillRepository;
        this.badgeRepository = badgeRepository;
        this.courseBadgeRepository = courseBadgeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.courseProviderRepository = courseProviderRepository;
        this.courseSectionRepository = courseSectionRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.courseReviewRepository = courseReviewRepository;
        this.giftVoucherRepository = giftVoucherRepository;
        this.coursePrerequisiteRepository = coursePrerequisiteRepository;
        this.courseCertificateRepository = courseCertificateRepository;
    }
    @Transactional(readOnly = true)
    public Page<CourseAdminResponse> getAllCourses(int page, int size, String status, String approvalStatus,
            String currentStage, String search, String createdBy, String instructor, String dateFrom, String dateTo,
            User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<Course> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean isAdmin = currentUser != null && currentUser.getRole() != null
                    && "ADMIN".equals(currentUser.getRole().getName());
            if (!isAdmin && currentUser != null) {
                var instructorsJoin = root.join("instructors", jakarta.persistence.criteria.JoinType.LEFT);
                var moderatorsJoin = root.join("moderators", jakarta.persistence.criteria.JoinType.LEFT);
                Predicate creatorMatch = cb.equal(root.get("createdBy").get("id"), currentUser.getId());
                Predicate instructorMatch = cb.and(
                        cb.isNotNull(instructorsJoin.get("id")),
                        cb.equal(instructorsJoin.get("id"), currentUser.getId()));
                Predicate moderatorMatch = cb.and(
                        cb.isNotNull(moderatorsJoin.get("id")),
                        cb.equal(moderatorsJoin.get("id"), currentUser.getId()));
                predicates.add(cb.or(creatorMatch, instructorMatch, moderatorMatch));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (approvalStatus != null) {
                if ("NULL".equals(approvalStatus) || approvalStatus.trim().isEmpty()) {
                    predicates.add(cb.isNull(root.get("approvalStatus")));
                } else {
                    predicates.add(cb.equal(root.get("approvalStatus"), approvalStatus));
                }
            }
            if (currentStage != null && !currentStage.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("currentStage"), currentStage));
            }
            if (createdBy != null && !createdBy.trim().isEmpty()) {
                var createdByJoin = root.join("createdBy", jakarta.persistence.criteria.JoinType.LEFT);
                String createdByPattern = "%" + createdBy.trim().toLowerCase() + "%";
                Predicate usernameMatch = cb.like(cb.lower(createdByJoin.get("username")), createdByPattern);
                Predicate emailMatch = cb.like(cb.lower(createdByJoin.get("email")), createdByPattern);
                predicates.add(cb.or(usernameMatch, emailMatch));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (instructor != null && !instructor.trim().isEmpty()) {
                var instructorsJoin = root.join("instructors", jakarta.persistence.criteria.JoinType.LEFT);
                String instructorPattern = "%" + instructor.trim().toLowerCase() + "%";
                Predicate usernameMatch = cb.like(cb.lower(instructorsJoin.get("username")), instructorPattern);
                Predicate emailMatch = cb.like(cb.lower(instructorsJoin.get("email")), instructorPattern);
                predicates.add(cb.or(usernameMatch, emailMatch));
                if (query != null) {
                    query.distinct(true);
                }
            }
            if (dateFrom != null && !dateFrom.trim().isEmpty()) {
                try {
                    LocalDateTime fromDate = LocalDateTime.parse(dateFrom + "T00:00:00");
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDate));
                } catch (Exception e) {
                }
            }
            if (dateTo != null && !dateTo.trim().isEmpty()) {
                try {
                    LocalDateTime toDate = LocalDateTime.parse(dateTo + "T23:59:59");
                    predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDate));
                } catch (Exception e) {
                }
            }
            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search.trim().toLowerCase() + "%";
                Predicate titlePred = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate slugPred = cb.like(cb.lower(root.get("slug")), searchPattern);
                Predicate idPred = null;
                try {
                    UUID searchUuid = UUID.fromString(search.trim());
                    idPred = cb.equal(root.get("id"), searchUuid);
                } catch (IllegalArgumentException e) {
                }
                if (idPred != null) {
                    predicates.add(cb.or(titlePred, slugPred, idPred));
                } else {
                    predicates.add(cb.or(titlePred, slugPred));
                }
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Course> courses = courseRepository.findAll(spec, pageable);
        return courses.map(this::mapToCourseAdminResponse);
    }
    @Transactional(readOnly = true)
    public CourseAdminResponse getCourseById(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (course.getCertificate() != null) {
            course.getCertificate().getId();  
        }
        if (course.getCourseBadges() == null) {
            course.setCourseBadges(new HashSet<>());
        } else {
            course.getCourseBadges().size();
        }
        if (course.getProviders() != null && !course.getProviders().isEmpty()) {
            course.getProviders().forEach(p -> p.getName());
        }
        if (course.getPrerequisites() == null) {
            course.setPrerequisites(new HashSet<>());
        } else {
            course.getPrerequisites().size();  
        }
        return mapToCourseAdminResponse(course);
    }
    @Transactional
    @SuppressWarnings("deprecation")
    public CourseAdminResponse createCourse(CourseCreateRequest request, User createdBy) {
        if (courseRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Course with slug '" + request.slug() + "' already exists.");
        }
        Course course = new Course();
        course.setTitle(request.title());
        course.setSlug(request.slug());
        course.setShortDescription(request.shortDescription());
        course.setFullDescription(request.fullDescription());
        course.setPrice(request.price());
        if (request.discountType() != null && request.discountValue() != null) {
            course.setDiscountType(request.discountType());
            course.setDiscountValue(request.discountValue());
            course.setDiscountIsFixed(request.discountIsFixed() != null ? request.discountIsFixed() : true);
            course.setDiscountDecayRate(request.discountDecayRate());
            course.calculateDiscountPrice();  
        } else if (request.discountPrice() != null) {
            course.setDiscountPrice(request.discountPrice());
            course.setDiscountType(null);
            course.setDiscountValue(null);
            course.setDiscountIsFixed(true);
            course.setDiscountDecayRate(null);
        } else {
            course.setDiscountPrice(null);
            course.setDiscountType(null);
            course.setDiscountValue(null);
            course.setDiscountIsFixed(true);
            course.setDiscountDecayRate(null);
        }
        course.setDiscountExpiresAt(request.discountExpiresAt());
        course.setCurrency(request.currency());
        course.setAccessType(request.accessType());
        course.setAccessDurationValue(request.accessDurationValue());
        course.setAccessDurationUnit(request.accessDurationUnit());
        course.setLevel(request.level());
        course.setDurationText(request.durationText());
        course.setCoverImageUrl(request.coverImageUrl());
        course.setLearningFormat(request.learningFormat());
        course.setLanguage(request.language());
        course.setSubtitlesLanguages(request.subtitlesLanguages());
        if (request.providerIds() != null && !request.providerIds().isEmpty()) {
            Set<CourseProvider> providers = new HashSet<>(courseProviderRepository.findAllById(request.providerIds()));
            if (providers.size() != request.providerIds().size()) {
                throw new IllegalArgumentException("One or more course providers not found");
            }
            course.setProviders(providers);
            if (!providers.isEmpty()) {
                CourseProvider firstProvider = providers.iterator().next();
                course.setOrganizationName(firstProvider.getName() != null ? firstProvider.getName() : "");
                course.setProviderLogoUrl(firstProvider.getLogoUrl());
            } else {
                course.setOrganizationName(request.organizationName() != null ? request.organizationName() : "");
                course.setProviderLogoUrl(request.providerLogoUrl());
            }
        } else {
            course.setProviders(new HashSet<>());
            course.setOrganizationName(
                    request.organizationName() != null && !request.organizationName().trim().isEmpty()
                            ? request.organizationName()
                            : "");
            course.setProviderLogoUrl(request.providerLogoUrl());
        }
        course.setHasFreeContent(request.hasFreeContent() != null ? request.hasFreeContent() : false);
        course.setAcademicDegree(request.academicDegree());
        course.setIsRefundable(request.isRefundable() != null ? request.isRefundable() : false);
        course.setHasDownloadableContent(
                request.hasDownloadableContent() != null ? request.hasDownloadableContent() : false);
        course.setVisibility(request.visibility() != null ? request.visibility() : "PUBLIC");
        course.setStatus("DRAFT");
        course.setCurrentStage("CORE_METADATA");
        course.setIsFeatured(false);
        course.setCreatedBy(createdBy);
        course.setApprovalStatus(null);
        if (request.instructorIds() != null && !request.instructorIds().isEmpty()) {
            Set<User> instructors = new HashSet<>(userRepository.findAllById(request.instructorIds()));
            if (instructors.size() != request.instructorIds().size()) {
                throw new IllegalArgumentException("One or more instructor IDs were not found.");
            }
            for (User instructor : instructors) {
                String roleName = instructor.getRole() != null ? instructor.getRole().getName() : null;
                if (roleName == null
                        || (!"INSTRUCTOR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName))) {
                    throw new IllegalArgumentException("User '" + instructor.getEmail() +
                            "' cannot be an instructor. User must have INSTRUCTOR or ADMIN role.");
                }
            }
            course.setInstructors(instructors);
        }
        if (request.allowedUserIds() != null && !request.allowedUserIds().isEmpty()) {
            Set<User> allowedUsers = new HashSet<>(userRepository.findAllById(request.allowedUserIds()));
            if (allowedUsers.size() != request.allowedUserIds().size()) {
                throw new IllegalArgumentException("One or more allowed user IDs were not found.");
            }
            course.setAllowedUsers(allowedUsers);
        } else {
            course.setAllowedUsers(new HashSet<>());
        }
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
            course.setCategories(categories);
        } else {
            course.setCategories(new HashSet<>());
        }
        if (request.tags() != null && !request.tags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            course.setTags(tags);
        }
        if (request.skills() != null && !request.skills().isEmpty()) {
            Set<Skill> skills = new HashSet<>();
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
            course.setSkills(skills);
        }
        Course savedCourse = courseRepository.save(course);
        if (request.certificate() != null) {
            CourseCertificate certificate;
            if (request.certificate().id() != null) {
                certificate = courseCertificateRepository.findById(request.certificate().id())
                        .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));
            } else {
                certificate = new CourseCertificate();
                certificate.setCourse(savedCourse);
            }
            if (request.certificate().slug() != null && !request.certificate().slug().trim().isEmpty()) {
                if (courseCertificateRepository.existsBySlug(request.certificate().slug().trim())) {
                    Optional<CourseCertificate> existingCert = courseCertificateRepository
                            .findBySlug(request.certificate().slug().trim());
                    if (existingCert.isPresent() && !existingCert.get().getId().equals(certificate.getId())) {
                        throw new IllegalArgumentException("Certificate slug already exists");
                    }
                }
                certificate.setSlug(request.certificate().slug().trim());
            }
            if (request.certificate().title() != null)
                certificate.setTitle(request.certificate().title());
            if (request.certificate().description() != null)
                certificate.setDescription(request.certificate().description());
            if (request.certificate().requirements() != null)
                certificate.setRequirements(request.certificate().requirements());
            if (request.certificate().minCompletionPercentage() != null) {
                certificate.setMinCompletionPercentage(request.certificate().minCompletionPercentage());
            }
            if (request.certificate().requiresInterview() != null) {
                certificate.setRequiresInterview(request.certificate().requiresInterview());
            }
            if (request.certificate().requiresSpecialExam() != null) {
                certificate.setRequiresSpecialExam(request.certificate().requiresSpecialExam());
            }
            if (request.certificate().examRequirements() != null) {
                certificate.setExamRequirements(request.certificate().examRequirements());
            }
            if (request.certificate().templateUrl() != null)
                certificate.setTemplateUrl(request.certificate().templateUrl());
            if (request.certificate().isActive() != null)
                certificate.setIsActive(request.certificate().isActive());
            if (request.certificate().validityMonths() != null)
                certificate.setValidityMonths(request.certificate().validityMonths());
            courseCertificateRepository.save(certificate);
        }
        if (request.badgeIds() != null && !request.badgeIds().isEmpty()) {
            courseBadgeRepository.deleteByCourseId(savedCourse.getId());
            List<Badge> badges = badgeRepository.findAllById(request.badgeIds());
            java.util.Map<String, java.util.List<Badge>> badgeNamesMap = new java.util.HashMap<>();
            for (Badge badge : badges) {
                if (badge.getName() != null && !badge.getName().trim().isEmpty()) {
                    String nameLower = badge.getName().toLowerCase().trim();
                    badgeNamesMap.computeIfAbsent(nameLower, k -> new java.util.ArrayList<>()).add(badge);
                }
            }
            java.util.List<String> duplicateNames = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, java.util.List<Badge>> entry : badgeNamesMap.entrySet()) {
                if (entry.getValue().size() > 1) {
                    duplicateNames.add(entry.getKey());
                }
            }
            if (!duplicateNames.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot assign multiple badges with the same name to the same course. " +
                                "Duplicate badge names: " + String.join(", ", duplicateNames));
            }
            // Validate that every badge is allowed to target COURSE
            for (Badge badge : badges) {
                String rawTargetType = badge.getTargetType();
                String allowedTargets = rawTargetType != null ? rawTargetType : "COURSE";
                boolean canTargetCourse = java.util.Arrays.stream(allowedTargets.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toUpperCase)
                        .anyMatch(t -> t.equals("COURSE"));
                if (!canTargetCourse) {
                    throw new IllegalStateException(
                            "Badge '" + badge.getName() + "' cannot be assigned to a course. " +
                            "It is configured for target type(s): " + allowedTargets);
                }
            }
            LocalDateTime now = LocalDateTime.now();
            for (Badge badge : badges) {
                CourseBadge courseBadge = new CourseBadge();
                courseBadge.setCourse(savedCourse);
                courseBadge.setBadge(badge);
                if (badge.getUsageDuration() != null && badge.getUsageDuration().toMinutes() > 0) {
                    LocalDateTime expirationDate = now.plus(badge.getUsageDuration());
                    courseBadge.setExpirationDate(expirationDate);
                } else if (badge.getExpirationDate() != null) {
                    courseBadge.setExpirationDate(badge.getExpirationDate());
                }
                CourseBadge savedCourseBadge = courseBadgeRepository.save(courseBadge);
                if (badge.getUsageDuration() != null && badge.getUsageDuration().toMinutes() > 0
                        && savedCourseBadge.getAssignedAt() != null) {
                    LocalDateTime accurateExpirationDate = savedCourseBadge.getAssignedAt()
                            .plus(badge.getUsageDuration());
                    savedCourseBadge.setExpirationDate(accurateExpirationDate);
                    courseBadgeRepository.save(savedCourseBadge);
                }
            }
        }
        if (request.prerequisites() != null && !request.prerequisites().isEmpty()) {
            coursePrerequisiteRepository.deleteByCourseId(savedCourse.getId());
            coursePrerequisiteRepository.flush();
            if (savedCourse.getPrerequisites() == null) {
                savedCourse.setPrerequisites(new HashSet<>());
            } else {
                savedCourse.getPrerequisites().clear();
            }
            for (com.loyalixa.backend.course.dto.PrerequisiteRequest prereq : request.prerequisites()) {
                if (prereq.type() == null || prereq.id() == null || prereq.requirementType() == null) {
                    continue;  
                }
                CoursePrerequisite coursePrereq = new CoursePrerequisite();
                coursePrereq.setCourse(savedCourse);
                coursePrereq.setPrerequisiteType(prereq.type().toUpperCase());  
                coursePrereq.setPrerequisiteId(prereq.id());
                coursePrereq.setRequirementType(prereq.requirementType().toUpperCase());  
                CoursePrerequisite savedPrereq = coursePrerequisiteRepository.save(coursePrereq);
                savedCourse.getPrerequisites().add(savedPrereq);
            }
        }
        return getCourseById(savedCourse.getId());
    }
    @Transactional
    @SuppressWarnings("deprecation")
    public CourseAdminResponse updateCourse(UUID courseId, CourseUpdateRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
        boolean isCreator = course.getCreatedBy() != null && course.getCreatedBy().getId().equals(currentUser.getId());
        boolean isModerator = course.getModerators() != null &&
                course.getModerators().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        if ("UNDER_REVIEW".equals(course.getApprovalStatus()) &&
                !isAdmin && !isCreator && !isModerator) {
            throw new IllegalStateException(
                    "Course is currently under active review and cannot be modified. Please wait for the review to complete.");
        }
        if (request.title() != null)
            course.setTitle(request.title());
        if (request.slug() != null && !request.slug().equals(course.getSlug())) {
            if (courseRepository.existsBySlug(request.slug())) {
                throw new IllegalStateException("Course with slug '" + request.slug() + "' already exists.");
            }
            course.setSlug(request.slug());
        }
        if (request.shortDescription() != null)
            course.setShortDescription(request.shortDescription());
        if (request.fullDescription() != null)
            course.setFullDescription(request.fullDescription());
        if (request.price() != null) {
            course.setPrice(request.price());
            if (course.getDiscountType() != null && course.getDiscountValue() != null) {
                course.calculateDiscountPrice();
            }
        }
        if (request.discountType() != null && request.discountValue() != null) {
            course.setDiscountType(request.discountType());
            course.setDiscountValue(request.discountValue());
            if (request.discountIsFixed() != null) {
                course.setDiscountIsFixed(request.discountIsFixed());
            }
            course.setDiscountDecayRate(request.discountDecayRate());
            course.calculateDiscountPrice();  
        } else if (request.discountPrice() != null) {
            course.setDiscountPrice(request.discountPrice());
            course.setDiscountType(null);
            course.setDiscountValue(null);
            course.setDiscountIsFixed(true);
            course.setDiscountDecayRate(null);
        } else if (request.discountType() == null && request.discountValue() == null
                && request.discountPrice() == null) {
            course.setDiscountPrice(null);
            course.setDiscountType(null);
            course.setDiscountValue(null);
            course.setDiscountIsFixed(true);
            course.setDiscountDecayRate(null);
        }
        if (request.discountExpiresAt() != null)
            course.setDiscountExpiresAt(request.discountExpiresAt());
        if (request.discountExpiresAt() == null && course.getDiscountPrice() == null) {
            course.setDiscountExpiresAt(null);
        }
        if (request.currency() != null)
            course.setCurrency(request.currency());
        if (request.accessType() != null)
            course.setAccessType(request.accessType());
        if (request.accessDurationValue() != null)
            course.setAccessDurationValue(request.accessDurationValue());
        if (request.accessDurationUnit() != null)
            course.setAccessDurationUnit(request.accessDurationUnit());
        if (request.level() != null)
            course.setLevel(request.level());
        if (request.durationText() != null)
            course.setDurationText(request.durationText());
        if (request.coverImageUrl() != null)
            course.setCoverImageUrl(request.coverImageUrl());
        if (request.status() != null && !"ARCHIVED".equals(request.status())) {
            course.setStatus(request.status());
        } else if (request.status() != null && "ARCHIVED".equals(request.status())) {
            throw new IllegalStateException(
                    "Cannot archive course through update endpoint. Please use /archive endpoint with course:archive permission.");
        }
        if (request.approvalStatus() != null) {
            String approvalStatus = request.approvalStatus();
            if ("null".equalsIgnoreCase(approvalStatus) || approvalStatus.trim().isEmpty()) {
                course.setApprovalStatus(null);
            } else {
                String statusUpper = approvalStatus.toUpperCase();
                if ("PENDING".equals(statusUpper) || "APPROVED".equals(statusUpper) || "REJECTED".equals(statusUpper)) {
                    course.setApprovalStatus(statusUpper);
                }
            }
        }
        if (request.currentStage() != null)
            course.setCurrentStage(request.currentStage());
        if (request.isFeatured() != null)
            course.setIsFeatured(request.isFeatured());
        if (request.learningFormat() != null)
            course.setLearningFormat(request.learningFormat());
        if (request.language() != null)
            course.setLanguage(request.language());
        if (request.subtitlesLanguages() != null)
            course.setSubtitlesLanguages(request.subtitlesLanguages());
        if (request.providerIds() != null) {
            if (request.providerIds().isEmpty()) {
                course.setProviders(new HashSet<>());
            } else {
                Set<CourseProvider> providers = new HashSet<>(
                        courseProviderRepository.findAllById(request.providerIds()));
                if (providers.size() != request.providerIds().size()) {
                    throw new IllegalArgumentException("One or more course providers not found");
                }
                course.setProviders(providers);
                if (!providers.isEmpty()) {
                    CourseProvider firstProvider = providers.iterator().next();
                    course.setOrganizationName(firstProvider.getName() != null ? firstProvider.getName() : "");
                    course.setProviderLogoUrl(firstProvider.getLogoUrl());
                } else {
                    if (course.getOrganizationName() == null) {
                        course.setOrganizationName("");
                    }
                }
            }
        }
        if (request.organizationName() != null) {
            course.setOrganizationName(request.organizationName());
        } else if (request.providerIds() == null || request.providerIds().isEmpty()) {
            if (course.getOrganizationName() == null) {
                course.setOrganizationName("");
            }
        }
        if (request.providerLogoUrl() != null)
            course.setProviderLogoUrl(request.providerLogoUrl());
        if (request.hasFreeContent() != null)
            course.setHasFreeContent(request.hasFreeContent());
        if (request.academicDegree() != null)
            course.setAcademicDegree(request.academicDegree());
        if (request.isRefundable() != null)
            course.setIsRefundable(request.isRefundable());
        if (request.hasDownloadableContent() != null)
            course.setHasDownloadableContent(request.hasDownloadableContent());
        if (request.visibility() != null)
            course.setVisibility(request.visibility());
        if (request.instructorIds() != null) {
            if (request.instructorIds().isEmpty()) {
                throw new IllegalArgumentException("Course must have at least one instructor.");
            } else {
                Set<User> instructors = new HashSet<>(userRepository.findAllById(request.instructorIds()));
                if (instructors.size() != request.instructorIds().size()) {
                    throw new IllegalArgumentException("One or more instructor IDs were not found.");
                }
                List<String> invalidInstructors = new ArrayList<>();
                for (User instructor : instructors) {
                    String roleName = instructor.getRole() != null ? instructor.getRole().getName() : null;
                    if (roleName == null
                            || (!"INSTRUCTOR".equalsIgnoreCase(roleName) && !"ADMIN".equalsIgnoreCase(roleName))) {
                        invalidInstructors
                                .add(instructor.getEmail() != null ? instructor.getEmail() : instructor.getUsername());
                    }
                }
                if (!invalidInstructors.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Some instructors do not have INSTRUCTOR or ADMIN role. Please remove them and add valid instructors. Invalid instructors: "
                                    + String.join(", ", invalidInstructors));
                }
                course.setInstructors(instructors);
            }
        }
        if (request.moderatorIds() != null) {
            if (request.moderatorIds().isEmpty()) {
                course.setModerators(new HashSet<>());
            } else {
                Set<User> moderators = new HashSet<>(userRepository.findAllById(request.moderatorIds()));
                if (moderators.size() != request.moderatorIds().size()) {
                    throw new IllegalArgumentException("One or more moderator IDs were not found.");
                }
                for (User moderator : moderators) {
                    String roleName = moderator.getRole() != null ? moderator.getRole().getName() : null;
                    if (roleName == null || (!"MODERATOR".equalsIgnoreCase(roleName) &&
                            !"INSTRUCTOR".equalsIgnoreCase(roleName) &&
                            !"ADMIN".equalsIgnoreCase(roleName))) {
                        throw new IllegalArgumentException("User '" + moderator.getEmail() +
                                "' cannot be a moderator. User must have MODERATOR, INSTRUCTOR, or ADMIN role.");
                    }
                }
                course.setModerators(moderators);
            }
        }
        if (request.allowedUserIds() != null) {
            if (request.allowedUserIds().isEmpty()) {
                course.setAllowedUsers(new HashSet<>());
            } else {
                Set<User> allowedUsers = new HashSet<>(userRepository.findAllById(request.allowedUserIds()));
                if (allowedUsers.size() != request.allowedUserIds().size()) {
                    throw new IllegalArgumentException("One or more allowed user IDs were not found.");
                }
                course.setAllowedUsers(allowedUsers);
            }
        }
        if (request.categoryIds() != null) {
            if (request.categoryIds().isEmpty()) {
                course.setCategories(new HashSet<>());
            } else {
                Set<Category> categories = new HashSet<>(categoryRepository.findAllById(request.categoryIds()));
                course.setCategories(categories);
            }
        }
        if (request.tags() != null) {
            Set<Tag> tags = new HashSet<>();
            for (String tagName : request.tags()) {
                Tag tag = tagRepository.findByNameIgnoreCase(tagName.trim())
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName.trim());
                            return tagRepository.save(newTag);
                        });
                tags.add(tag);
            }
            course.setTags(tags);
        }
        if (request.skills() != null) {
            Set<Skill> skills = new HashSet<>();
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
            course.setSkills(skills);
        }
        Course updatedCourse = courseRepository.save(course);
        if (request.certificate() != null) {
            CourseCertificate certificate;
            Optional<CourseCertificate> existingCert = courseCertificateRepository
                    .findByCourseId(updatedCourse.getId());
            if (existingCert.isPresent()) {
                certificate = existingCert.get();
            } else {
                certificate = new CourseCertificate();
                certificate.setCourse(updatedCourse);
            }
            if (request.certificate().slug() != null && !request.certificate().slug().trim().isEmpty()) {
                if (courseCertificateRepository.existsBySlug(request.certificate().slug().trim())) {
                    Optional<CourseCertificate> slugCert = courseCertificateRepository
                            .findBySlug(request.certificate().slug().trim());
                    if (slugCert.isPresent() && !slugCert.get().getId().equals(certificate.getId())) {
                        throw new IllegalArgumentException("Certificate slug already exists");
                    }
                }
                certificate.setSlug(request.certificate().slug().trim());
            }
            if (request.certificate().title() != null)
                certificate.setTitle(request.certificate().title());
            if (request.certificate().description() != null)
                certificate.setDescription(request.certificate().description());
            if (request.certificate().requirements() != null)
                certificate.setRequirements(request.certificate().requirements());
            if (request.certificate().minCompletionPercentage() != null) {
                certificate.setMinCompletionPercentage(request.certificate().minCompletionPercentage());
            }
            if (request.certificate().requiresInterview() != null) {
                certificate.setRequiresInterview(request.certificate().requiresInterview());
            }
            if (request.certificate().requiresSpecialExam() != null) {
                certificate.setRequiresSpecialExam(request.certificate().requiresSpecialExam());
            }
            if (request.certificate().examRequirements() != null) {
                certificate.setExamRequirements(request.certificate().examRequirements());
            }
            if (request.certificate().templateUrl() != null)
                certificate.setTemplateUrl(request.certificate().templateUrl());
            if (request.certificate().isActive() != null)
                certificate.setIsActive(request.certificate().isActive());
            if (request.certificate().validityMonths() != null)
                certificate.setValidityMonths(request.certificate().validityMonths());
            courseCertificateRepository.save(certificate);
        } else {
            Optional<CourseCertificate> existingCert = courseCertificateRepository
                    .findByCourseId(updatedCourse.getId());
            if (existingCert.isPresent()) {
                courseCertificateRepository.delete(existingCert.get());
            }
        }
        if (request.prerequisites() != null) {
            coursePrerequisiteRepository.deleteByCourseId(updatedCourse.getId());
            coursePrerequisiteRepository.flush();
            if (updatedCourse.getPrerequisites() != null) {
                updatedCourse.getPrerequisites().clear();
            } else {
                updatedCourse.setPrerequisites(new HashSet<>());
            }
            if (!request.prerequisites().isEmpty()) {
                for (com.loyalixa.backend.course.dto.PrerequisiteRequest prereq : request.prerequisites()) {
                    if (prereq.type() == null || prereq.id() == null || prereq.requirementType() == null) {
                        continue;  
                    }
                    CoursePrerequisite coursePrereq = new CoursePrerequisite();
                    coursePrereq.setCourse(updatedCourse);
                    coursePrereq.setPrerequisiteType(prereq.type().toUpperCase());  
                    coursePrereq.setPrerequisiteId(prereq.id());
                    coursePrereq.setRequirementType(prereq.requirementType().toUpperCase());  
                    CoursePrerequisite savedPrereq = coursePrerequisiteRepository.save(coursePrereq);
                    updatedCourse.getPrerequisites().add(savedPrereq);
                }
            }
        }
        if (request.badgeIds() != null) {
            courseBadgeRepository.deleteByCourseId(updatedCourse.getId());
            if (!request.badgeIds().isEmpty()) {
                List<Badge> badges = badgeRepository.findAllById(request.badgeIds());
                java.util.Map<String, java.util.List<Badge>> badgeNamesMap = new java.util.HashMap<>();
                for (Badge badge : badges) {
                    if (badge.getName() != null && !badge.getName().trim().isEmpty()) {
                        String nameLower = badge.getName().toLowerCase().trim();
                        badgeNamesMap.computeIfAbsent(nameLower, k -> new java.util.ArrayList<>()).add(badge);
                    }
                }
                java.util.List<String> duplicateNames = new java.util.ArrayList<>();
                for (java.util.Map.Entry<String, java.util.List<Badge>> entry : badgeNamesMap.entrySet()) {
                    if (entry.getValue().size() > 1) {
                        duplicateNames.add(entry.getKey());
                    }
                }
                if (!duplicateNames.isEmpty()) {
                    throw new IllegalStateException(
                            "Cannot assign multiple badges with the same name to the same course. " +
                                    "Duplicate badge names: " + String.join(", ", duplicateNames));
                }
                // Validate that every badge is allowed to target COURSE
                for (Badge badge : badges) {
                    String rawTargetType = badge.getTargetType();
                    String allowedTargets = rawTargetType != null ? rawTargetType : "COURSE";
                    boolean canTargetCourse = java.util.Arrays.stream(allowedTargets.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(String::toUpperCase)
                            .anyMatch(t -> t.equals("COURSE"));
                    if (!canTargetCourse) {
                        throw new IllegalStateException(
                                "Badge '" + badge.getName() + "' cannot be assigned to a course. " +
                                "It is configured for target type(s): " + allowedTargets);
                    }
                }
                LocalDateTime now = LocalDateTime.now();
                for (Badge badge : badges) {
                    CourseBadge courseBadge = new CourseBadge();
                    courseBadge.setCourse(updatedCourse);
                    courseBadge.setBadge(badge);
                    if (badge.getUsageDuration() != null && badge.getUsageDuration().toMinutes() > 0) {
                        LocalDateTime expirationDate = now.plus(badge.getUsageDuration());
                        courseBadge.setExpirationDate(expirationDate);
                    } else if (badge.getExpirationDate() != null) {
                        courseBadge.setExpirationDate(badge.getExpirationDate());
                    }
                    CourseBadge savedCourseBadge = courseBadgeRepository.save(courseBadge);
                    if (badge.getUsageDuration() != null && badge.getUsageDuration().toMinutes() > 0
                            && savedCourseBadge.getAssignedAt() != null) {
                        LocalDateTime accurateExpirationDate = savedCourseBadge.getAssignedAt()
                                .plus(badge.getUsageDuration());
                        savedCourseBadge.setExpirationDate(accurateExpirationDate);
                        courseBadgeRepository.save(savedCourseBadge);
                    }
                }
            }
        }
        return getCourseById(updatedCourse.getId());
    }
    @Transactional
    public CourseAdminResponse updateApprovalStatus(UUID courseId, CourseApprovalRequest request, User adminUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        String oldStatus = course.getStatus();
        String oldApprovalStatus = course.getApprovalStatus();
        boolean isAdmin = adminUser.getRole() != null && "ADMIN".equals(adminUser.getRole().getName());
        if (!isAdmin) {
            throw new IllegalStateException("Only ADMIN can change approval status.");
        }
        if (request.approvalStatus() == null || request.approvalStatus().trim().isEmpty()
                || "null".equalsIgnoreCase(request.approvalStatus())) {
            course.setApprovalStatus(null);
            course.setIsUnderReview(false);  
            if (!"ARCHIVED".equals(course.getStatus())) {
                course.setStatus("DRAFT");
            }
        } else {
            String status = request.approvalStatus().toUpperCase();
            if (!"APPROVED".equals(status) && !"REJECTED".equals(status) &&
                    !"REVIEW_QUEUE".equals(status) && !"UNDER_REVIEW".equals(status)) {
                throw new IllegalArgumentException("Invalid approval status: " + status
                        + ". Valid values: REVIEW_QUEUE, UNDER_REVIEW, APPROVED, REJECTED");
            }
            course.setApprovalStatus(status);
            if ("APPROVED".equals(status)) {
                course.setStatus("PUBLISHED");
                course.setIsUnderReview(false);  
                if (course.getApprovedAt() == null) {
                    course.setApprovedAt(LocalDateTime.now());
                }
                System.out.println(
                        "[CourseAdminService] Course approved - Status changed from " + oldStatus + " to PUBLISHED");
            } else if ("REJECTED".equals(status)) {
                course.setIsUnderReview(false);  
                if (!"ARCHIVED".equals(course.getStatus())) {
                    course.setStatus("DRAFT");
                    System.out.println(
                            "[CourseAdminService] Course rejected - Status changed from " + oldStatus + " to DRAFT");
                }
            } else if ("UNDER_REVIEW".equals(status)) {
                course.setIsUnderReview(true);
            } else if ("REVIEW_QUEUE".equals(status)) {
                course.setIsUnderReview(false);
            }
        }
        courseRepository.save(course);
        courseRepository.flush();  
        Course updatedCourse = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found after update: " + courseId));
        System.out.println("[CourseAdminService] Course updated - ID: " + courseId +
                ", Status: " + oldStatus + " -> " + updatedCourse.getStatus() +
                ", ApprovalStatus: " + oldApprovalStatus + " -> " + updatedCourse.getApprovalStatus());
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional
    public CourseAdminResponse updateStage(UUID courseId, CourseStageUpdateRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
        boolean isCreator = course.getCreatedBy() != null && course.getCreatedBy().getId().equals(currentUser.getId());
        boolean isModerator = course.getModerators() != null &&
                course.getModerators().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        boolean isInstructor = course.getInstructors() != null &&
                course.getInstructors().stream().anyMatch(i -> i.getId().equals(currentUser.getId()));
        if (!isAdmin && !isCreator && !isModerator && !isInstructor) {
            throw new IllegalStateException("User does not have permission to update this course stage.");
        }
        if ("PENDING".equals(course.getApprovalStatus()) && !isAdmin) {
            throw new IllegalStateException("Course is pending approval and cannot be modified.");
        }
        if (request.currentStage() != null) {
            String stage = request.currentStage().toUpperCase();
            List<String> validStages = Arrays.asList("CORE_METADATA", "TAXONOMY_RESOURCES", "CONTENT_STRUCTURE",
                    "REVIEW_LAUNCH", "FINISHED");
            if (!validStages.contains(stage)) {
                throw new IllegalArgumentException("Invalid stage: " + stage);
            }
            course.setCurrentStage(stage);
        }
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional(readOnly = true)
    public CourseValidationResponse validateCourse(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        List<String> missingFields = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        if (course.getTitle() == null || course.getTitle().trim().isEmpty()) {
            missingFields.add("Course Title");
        }
        if (course.getSlug() == null || course.getSlug().trim().isEmpty()) {
            missingFields.add("Course Slug");
        }
        if (course.getPrice() == null) {
            missingFields.add("Course Price");
        }
        if (course.getShortDescription() == null || course.getShortDescription().trim().isEmpty()) {
            warnings.add("Short Description is recommended");
        }
        if (course.getFullDescription() == null || course.getFullDescription().trim().isEmpty()) {
            warnings.add("Full Description is recommended");
        }
        if (course.getCoverImageUrl() == null || course.getCoverImageUrl().trim().isEmpty()) {
            warnings.add("Cover Image URL is recommended");
        }
        if (course.getLevel() == null || course.getLevel().trim().isEmpty()) {
            warnings.add("Course Level is recommended");
        }
        if (course.getInstructors() == null || course.getInstructors().isEmpty()) {
            missingFields.add("At least one Instructor");
        }
        if (course.getCategories() == null || course.getCategories().isEmpty()) {
            warnings.add("At least one Category is recommended");
        }
        long sectionsCount = courseSectionRepository.countByCourseId(courseId);
        if (sectionsCount == 0) {
            warnings.add("At least one Module/Section is recommended");
        } else {
            List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
            long totalLessons = 0;
            for (CourseSection section : sections) {
                long lessonCount = lessonRepository.countBySectionId(section.getId());
                totalLessons += lessonCount;
            }
            if (totalLessons == 0) {
                warnings.add("At least one Lesson is recommended");
            }
        }
        if ("PRIVATE".equals(course.getVisibility()) &&
                (course.getAllowedUsers() == null || course.getAllowedUsers().isEmpty())) {
            missingFields.add("Allowed Users (required for PRIVATE visibility)");
        }
        boolean isValid = missingFields.isEmpty();
        return new CourseValidationResponse(
                courseId,
                isValid,
                missingFields,
                warnings,
                course.getTitle(),
                course.getStatus(),
                course.getApprovalStatus());
    }
    @Transactional
    public CourseAdminResponse submitForReview(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
        boolean isCreator = course.getCreatedBy() != null && course.getCreatedBy().getId().equals(currentUser.getId());
        boolean isModerator = course.getModerators() != null &&
                course.getModerators().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        boolean isInstructor = course.getInstructors() != null &&
                course.getInstructors().stream().anyMatch(i -> i.getId().equals(currentUser.getId()));
        if (!isAdmin && !isCreator && !isModerator && !isInstructor) {
            throw new IllegalStateException("User does not have permission to submit this course for review.");
        }
        if (("REVIEW_QUEUE".equals(course.getApprovalStatus()) || "UNDER_REVIEW".equals(course.getApprovalStatus()))
                && !isAdmin) {
            throw new IllegalStateException("Course is already in review queue or under review.");
        }
        CourseValidationResponse validation = validateCourse(courseId);
        if (!validation.isValid()) {
            throw new IllegalStateException("Course cannot be submitted. Missing required fields: " +
                    String.join(", ", validation.missingFields()));
        }
        course.setApprovalStatus("REVIEW_QUEUE");
        course.setIsUnderReview(false);  
        course.setCurrentStage("FINISHED");
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional
    public CourseAdminResponse withdrawFromReview(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
        boolean isCreator = course.getCreatedBy() != null && course.getCreatedBy().getId().equals(currentUser.getId());
        boolean isModerator = course.getModerators() != null &&
                course.getModerators().stream().anyMatch(m -> m.getId().equals(currentUser.getId()));
        boolean isInstructor = course.getInstructors() != null &&
                course.getInstructors().stream().anyMatch(i -> i.getId().equals(currentUser.getId()));
        if (!isAdmin && !isCreator && !isModerator && !isInstructor) {
            throw new IllegalStateException("User does not have permission to withdraw this course from review.");
        }
        if (!"REVIEW_QUEUE".equals(course.getApprovalStatus())) {
            if ("UNDER_REVIEW".equals(course.getApprovalStatus())) {
                throw new IllegalStateException(
                        "Cannot withdraw course. The course is currently under active review. Please wait for the review to complete.");
            }
            throw new IllegalStateException(
                    "Course is not in review queue. Current approval status: " + course.getApprovalStatus());
        }
        course.setApprovalStatus(null);
        course.setIsUnderReview(false);
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional
    public CourseAdminResponse startReview(UUID courseId, User adminUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = adminUser.getRole() != null && "ADMIN".equals(adminUser.getRole().getName());
        if (!isAdmin) {
            throw new IllegalStateException("Only ADMIN can start reviewing a course.");
        }
        if (!"REVIEW_QUEUE".equals(course.getApprovalStatus())) {
            throw new IllegalStateException(
                    "Cannot start review. Course must be in review queue (REVIEW_QUEUE). Current approval status: "
                            + course.getApprovalStatus());
        }
        course.setApprovalStatus("UNDER_REVIEW");
        course.setIsUnderReview(true);
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional
    public CourseAdminResponse updateAdminRatingPoints(UUID courseId,
            com.loyalixa.backend.course.dto.AdminRatingPointsRequest request, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (currentUser.getRole() == null || !"ADMIN".equals(currentUser.getRole().getName())) {
            throw new IllegalStateException("Only ADMIN users can update admin rating points.");
        }
        if (request.points() == null || request.points() < 0) {
            throw new IllegalArgumentException("Rating points must be a non-negative integer.");
        }
        if (request.points() == 0) {
            course.setAdminRatingPoints(0);
            course.setRatingPointsIsFixed(true);
            course.setRatingPointsExpiresAt(null);
        } else {
            course.setAdminRatingPoints(request.points());
            if (request.isFixed() != null) {
                course.setRatingPointsIsFixed(request.isFixed());
            } else {
                course.setRatingPointsIsFixed(true);  
            }
            if (!course.getRatingPointsIsFixed()) {
                if (request.expiresAt() == null) {
                    throw new IllegalArgumentException(
                            "Expiration date is required when rating points are not fixed (decreasing over time).");
                }
                if (request.expiresAt().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("Expiration date must be in the future.");
                }
                course.setRatingPointsExpiresAt(request.expiresAt());
            } else {
                course.setRatingPointsExpiresAt(null);
            }
        }
        Course updatedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(updatedCourse);
    }
    @Transactional
    public CourseAdminResponse archiveCourse(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if ("ARCHIVED".equals(course.getStatus())) {
            throw new IllegalStateException("Course is already archived.");
        }
        course.setStatus("ARCHIVED");
        course.setArchivedAt(LocalDateTime.now());
        Course savedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(savedCourse);
    }
    @Transactional
    public CourseAdminResponse unarchiveCourse(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        if (!"ARCHIVED".equals(course.getStatus())) {
            throw new IllegalStateException("Course is not archived. Current status: " + course.getStatus());
        }
        String newStatus = course.getApprovalStatus() != null && "APPROVED".equals(course.getApprovalStatus())
                ? "PUBLISHED"
                : "DRAFT";
        course.setStatus(newStatus);
        course.setUnarchivedAt(LocalDateTime.now());
        Course savedCourse = courseRepository.save(course);
        return mapToCourseAdminResponse(savedCourse);
    }
    @Transactional
    public void deleteCourse(UUID courseId, User currentUser) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        boolean isAdmin = currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getName());
        if (!isAdmin) {
            throw new IllegalStateException("Only ADMIN can delete courses.");
        }
        String courseStatus = course.getStatus();
        long enrollmentCount = enrollmentRepository.countByCourseId(courseId);
        if ("PUBLISHED".equals(courseStatus) && enrollmentCount > 0) {
            throw new IllegalStateException("Cannot delete published course. There are " + enrollmentCount +
                    " student(s) enrolled in this course. Please archive the course first or remove all enrollments.");
        }
        courseRepository.deleteInstructorsLinks(courseId);
        courseRepository.deleteModeratorsLinks(courseId);
        courseRepository.deleteAllowedUsersLinks(courseId);
        courseRepository.deleteTagsLinks(courseId);
        courseRepository.deleteCategoriesLinks(courseId);
        courseRepository.deleteSkillsLinks(courseId);
        courseRepository.deleteBundleLinks(courseId);
        courseRepository.deleteProvidersLinks(courseId);
        courseRepository.flush();
        coursePrerequisiteRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        courseBadgeRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        courseCertificateRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        courseReviewRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        if (!"PUBLISHED".equals(courseStatus) || enrollmentCount == 0) {
            enrollmentRepository.deleteByCourseId(courseId);
            courseRepository.flush();
        }
        discountCodeRepository.deleteCourseLinks(courseId);
        courseRepository.flush();
        giftVoucherRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        quizRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        for (CourseSection section : sections) {
            lessonRepository.unlinkQuizzesFromLessonsBySection(section.getId());
            lessonRepository.deleteBySectionId(section.getId());
        }
        courseRepository.flush();
        courseSectionRepository.deleteByCourseId(courseId);
        courseRepository.flush();
        courseRepository.deleteCourseById(courseId);
    }
    @Transactional(readOnly = true)
    public List<SectionResponse> getCourseSections(UUID courseId) {
        if (!courseRepository.existsById(courseId)) {
            throw new IllegalArgumentException("Course not found: " + courseId);
        }
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return sections.stream()
                .map(section -> new SectionResponse(
                        section.getId(),
                        section.getTitle(),
                        section.getOrderIndex(),
                        section.getIsFreePreview(),
                        section.getCourse().getId()))
                .collect(Collectors.toList());
    }
    @Transactional
    public SectionResponse createCourseSection(UUID courseId, SectionRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));
        CourseSection section = new CourseSection();
        section.setTitle(request.title());
        section.setOrderIndex(request.orderIndex());
        section.setIsFreePreview(request.isFreePreview() != null ? request.isFreePreview() : false);
        section.setCourse(course);
        CourseSection savedSection = courseSectionRepository.save(section);
        if (savedSection.getIsFreePreview()) {
            course.setHasFreeContent(true);
            courseRepository.save(course);
        }
        return new SectionResponse(
                savedSection.getId(),
                savedSection.getTitle(),
                savedSection.getOrderIndex(),
                savedSection.getIsFreePreview(),
                savedSection.getCourse().getId());
    }
    @Transactional
    public SectionResponse updateCourseSection(UUID courseId, Long sectionId, SectionRequest request) {
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        if (!section.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Section does not belong to this course.");
        }
        section.setTitle(request.title());
        section.setOrderIndex(request.orderIndex());
        if (request.isFreePreview() != null) {
            section.setIsFreePreview(request.isFreePreview());
        }
        CourseSection savedSection = courseSectionRepository.save(section);
        Course course = section.getCourse();
        boolean hasFreeContent = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .anyMatch(s -> s.getIsFreePreview());
        course.setHasFreeContent(hasFreeContent);
        courseRepository.save(course);
        return new SectionResponse(
                savedSection.getId(),
                savedSection.getTitle(),
                savedSection.getOrderIndex(),
                savedSection.getIsFreePreview(),
                savedSection.getCourse().getId());
    }
    @Transactional
    public void deleteCourseSection(UUID courseId, Long sectionId) {
        CourseSection section = courseSectionRepository.findById(sectionId)
                .orElseThrow(() -> new IllegalArgumentException("Section not found: " + sectionId));
        if (!section.getCourse().getId().equals(courseId)) {
            throw new IllegalArgumentException("Section does not belong to this course.");
        }
        Course course = section.getCourse();
        courseSectionRepository.delete(section);
        boolean hasFreeContent = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .anyMatch(s -> s.getIsFreePreview());
        course.setHasFreeContent(hasFreeContent);
        courseRepository.save(course);
    }
    @SuppressWarnings("deprecation")
    private CourseAdminResponse mapToCourseAdminResponse(Course course) {
        return new CourseAdminResponse(
                course.getId(),
                course.getTitle(),
                course.getSlug(),
                course.getShortDescription(),
                course.getFullDescription(),
                course.getPrice(),
                course.getDiscountPrice(),
                course.getDiscountType(),
                course.getDiscountValue(),
                course.getDiscountIsFixed() != null ? course.getDiscountIsFixed() : true,
                course.getDiscountDecayRate(),
                course.getDiscountExpiresAt(),
                course.getCurrency(),
                course.getAccessType(),
                course.getAccessDurationValue(),
                course.getAccessDurationUnit(),
                course.getLevel(),
                course.getDurationText(),
                course.getCoverImageUrl(),
                course.getStatus(),
                course.getApprovalStatus(),
                course.getIsUnderReview() != null ? course.getIsUnderReview() : false,
                course.getCurrentStage(),
                course.getIsFeatured(),
                course.getLearningFormat(),
                course.getLanguage(),
                course.getSubtitlesLanguages(),
                course.getAcademicDegree(),
                course.getIsRefundable() != null ? course.getIsRefundable() : false,
                course.getHasDownloadableContent() != null ? course.getHasDownloadableContent() : false,
                course.getAdminRatingPoints() != null ? course.getAdminRatingPoints() : 0,
                course.getRatingPointsIsFixed() != null ? course.getRatingPointsIsFixed() : true,
                course.getRatingPointsExpiresAt(),
                course.getCertificate() != null ? new CourseAdminResponse.CertificateInfo(
                        course.getCertificate().getId(),
                        course.getCertificate().getSlug(),
                        course.getCertificate().getTitle(),
                        course.getCertificate().getDescription(),
                        course.getCertificate().getRequirements(),
                        course.getCertificate().getMinCompletionPercentage(),
                        course.getCertificate().getRequiresInterview(),
                        course.getCertificate().getRequiresSpecialExam(),
                        course.getCertificate().getExamRequirements(),
                        course.getCertificate().getTemplateUrl(),
                        course.getCertificate().getIsActive(),
                        course.getCertificate().getValidityMonths()) : null,
                course.getProviders() != null && !course.getProviders().isEmpty()
                        ? course.getProviders().stream()
                                .map(p -> new CourseAdminResponse.ProviderInfo(
                                        p.getId(),
                                        p.getName(),
                                        p.getLogoUrl(),
                                        p.getWebsiteUrl()))
                                .toList()
                        : List.of(),
                course.getOrganizationName(),  
                course.getProviderLogoUrl(),  
                course.getHasFreeContent(),
                course.getVisibility() != null ? course.getVisibility() : "PUBLIC",
                course.getCreatedBy() != null ? course.getCreatedBy().getId() : null,
                course.getCreatedBy() != null ? course.getCreatedBy().getUsername() : null,
                course.getCreatedBy() != null ? course.getCreatedBy().getEmail() : null,
                course.getInstructors() != null ? course.getInstructors().stream()
                        .map(i -> new CourseAdminResponse.InstructorInfo(
                                i.getId(),
                                i.getUsername(),
                                i.getEmail(),
                                i.getRole() != null ? i.getRole().getName() : null))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getModerators() != null ? course.getModerators().stream()
                        .map(m -> new CourseAdminResponse.ModeratorInfo(
                                m.getId(),
                                m.getUsername(),
                                m.getEmail(),
                                m.getRole() != null ? m.getRole().getName() : null))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getAllowedUsers() != null ? course.getAllowedUsers().stream()
                        .map(u -> new CourseAdminResponse.AllowedUserInfo(u.getId(), u.getUsername(), u.getEmail()))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getCategories() != null ? course.getCategories().stream()
                        .map(c -> new CourseAdminResponse.CategoryInfo(c.getId(), c.getName(), c.getSlug()))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getTags() != null ? course.getTags().stream()
                        .map(t -> new CourseAdminResponse.TagInfo(t.getId(), t.getName()))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getSkills() != null ? course.getSkills().stream()
                        .map(s -> new CourseAdminResponse.SkillInfo(s.getId(), s.getName()))
                        .collect(Collectors.toList()) : Collections.emptyList(),
                course.getCourseBadges() != null && !course.getCourseBadges().isEmpty()
                        ? course.getCourseBadges().stream()
                                .filter(cb -> cb != null && cb.getBadge() != null)
                                .filter(cb -> {
                                    LocalDateTime expirationDate = cb.getExpirationDate();
                                    if (expirationDate == null) {
                                        return true;
                                    }
                                    return expirationDate.isAfter(LocalDateTime.now());
                                })
                                .map(cb -> {
                                    Badge badge = cb.getBadge();
                                    Long usageDurationMinutes = null;
                                    if (badge.getUsageDuration() != null) {
                                        usageDurationMinutes = badge.getUsageDuration().toMinutes();
                                    }
                                    return new CourseAdminResponse.BadgeInfo(
                                            badge.getId(),
                                            badge.getName(),
                                            badge.getColorCode(),
                                            badge.getIconClass(),
                                            badge.getExpirationDate(),
                                            usageDurationMinutes,
                                            cb.getExpirationDate(),
                                            cb.getAssignedAt());
                                })
                                .collect(Collectors.toList())
                        : Collections.emptyList(),
                course.getPrerequisites() != null && !course.getPrerequisites().isEmpty()
                        ? course.getPrerequisites().stream()
                                .map(prereq -> {
                                    String name = null;
                                    if ("SKILL".equals(prereq.getPrerequisiteType())) {
                                        try {
                                            UUID skillId = UUID.fromString(prereq.getPrerequisiteId());
                                            Skill skill = skillRepository.findById(skillId).orElse(null);
                                            name = skill != null ? skill.getName() : "Unknown Skill";
                                        } catch (Exception e) {
                                            name = "Invalid Skill ID";
                                        }
                                    } else if ("COURSE".equals(prereq.getPrerequisiteType())) {
                                        try {
                                            UUID courseId = UUID.fromString(prereq.getPrerequisiteId());
                                            Course prereqCourse = courseRepository.findById(courseId).orElse(null);
                                            name = prereqCourse != null ? prereqCourse.getTitle() : "Unknown Course";
                                        } catch (Exception e) {
                                            name = "Invalid Course ID";
                                        }
                                    }
                                    return new CourseAdminResponse.PrerequisiteInfo(
                                            prereq.getPrerequisiteType(),
                                            prereq.getPrerequisiteId(),
                                            name != null ? name : "Unknown",
                                            prereq.getRequirementType());
                                })
                                .collect(Collectors.toList())
                        : Collections.emptyList(),
                calculateCourseStatistics(course.getId()),
                course.getCreatedAt(),
                course.getUpdatedAt(),
                course.getApprovedAt(),
                course.getArchivedAt(),
                course.getUnarchivedAt());
    }
    @Transactional(readOnly = true)
    private CourseAdminResponse.CourseStatistics calculateCourseStatistics(UUID courseId) {
        long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        long giftReceiversCount = giftVoucherRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.equal(root.get("status"), "REDEEMED")))
                .size();
        long giftSendersCount = giftVoucherRepository.findAll(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("course").get("id"), courseId),
                        cb.or(
                                cb.equal(root.get("status"), "ISSUED"),
                                cb.equal(root.get("status"), "REDEEMED"))))
                .size();
        List<CourseSection> sections = courseSectionRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        long totalModules = sections.size();
        long totalLessons = 0;
        List<CourseAdminResponse.CourseStatistics.ModuleInfo> modulesInfo = new ArrayList<>();
        for (CourseSection section : sections) {
            long lessonsCount = lessonRepository.countBySectionId(section.getId());
            totalLessons += lessonsCount;
            modulesInfo.add(new CourseAdminResponse.CourseStatistics.ModuleInfo(
                    section.getId(),
                    section.getTitle(),
                    section.getOrderIndex(),
                    lessonsCount,
                    section.getIsFreePreview()));
        }
        List<Quiz> quizzes = new ArrayList<>();
        for (CourseSection section : sections) {
            List<Lesson> lessons = lessonRepository.findBySectionIdOrderByOrderIndexAsc(section.getId());
            for (Lesson lesson : lessons) {
                if (lesson.getQuiz() != null) {
                    quizzes.add(lesson.getQuiz());
                }
            }
        }
        int totalCoursePoints = quizzes.stream()
                .mapToInt(q -> q.getQuestions() != null ? q.getQuestions().stream()
                        .mapToInt(question -> question.getPoints() != null ? question.getPoints() : 0)
                        .sum() : 0)
                .sum();
        double averageStudentScore = 0.0;
        List<com.loyalixa.backend.discount.DiscountCode> discountCodes = discountCodeRepository.findAll()
                .stream()
                .filter(dc -> dc.getApplicableCourses() != null &&
                        dc.getApplicableCourses().stream()
                                .anyMatch(c -> c.getId().equals(courseId)))
                .collect(Collectors.toList());
        List<CourseAdminResponse.CourseStatistics.DiscountCodeInfo> discountCodesInfo = discountCodes.stream()
                .map(dc -> {
                    String permissions = "";
                    if (dc.getIsPrivate() != null && dc.getIsPrivate()) {
                        permissions += "Private (Limited users)";
                    } else {
                        permissions += "Public";
                    }
                    if (dc.getValidUntil() != null) {
                        permissions += ", Expires: " + dc.getValidUntil().toString();
                    }
                    if (dc.getMaxUses() != null) {
                        permissions += ", Max uses: " + dc.getMaxUses();
                    }
                    return new CourseAdminResponse.CourseStatistics.DiscountCodeInfo(
                            dc.getId(),
                            dc.getCode(),
                            dc.getDiscountType(),
                            dc.getDiscountValue(),
                            dc.getCurrentUses() != null ? dc.getCurrentUses() : 0,
                            dc.getMaxUses(),
                            dc.getValidUntil(),
                            dc.getIsPrivate() != null ? dc.getIsPrivate() : false,
                            permissions);
                })
                .collect(Collectors.toList());
        return new CourseAdminResponse.CourseStatistics(
                totalEnrollments,
                giftReceiversCount,
                giftSendersCount,
                totalModules,
                totalLessons,
                modulesInfo,
                totalCoursePoints,
                averageStudentScore,
                discountCodesInfo);
    }
}