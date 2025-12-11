package com.loyalixa.backend.discount;
import com.loyalixa.backend.discount.dto.DiscountRequest;
import com.loyalixa.backend.discount.dto.DiscountSearchRequest;
import com.loyalixa.backend.course.Course;
import com.loyalixa.backend.course.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loyalixa.backend.discount.dto.DiscountResponse;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.discount.dto.DiscountDetailsResponse;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.ArrayList;
@Service
public class DiscountService {
    private final DiscountCodeRepository discountRepository;
    private final CourseRepository courseRepository;
    private final com.loyalixa.backend.shop.ProductRepository productRepository;
    private final UserDiscountRepository userDiscountRepository;
    private final com.loyalixa.backend.user.UserRepository userRepository;
    private final com.loyalixa.backend.subscription.UserSubscriptionRepository userSubscriptionRepository;
    public DiscountService(DiscountCodeRepository discountRepository, CourseRepository courseRepository, 
                          com.loyalixa.backend.shop.ProductRepository productRepository,
                          UserDiscountRepository userDiscountRepository, com.loyalixa.backend.user.UserRepository userRepository,
                          com.loyalixa.backend.subscription.UserSubscriptionRepository userSubscriptionRepository) {
        this.discountRepository = discountRepository;
        this.courseRepository = courseRepository;
        this.productRepository = productRepository;
        this.userDiscountRepository = userDiscountRepository;
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
    }
    @Transactional
    public DiscountCode createDiscountCode(DiscountRequest request, User adminUser) {
        if (discountRepository.findByCodeIgnoreCase(request.code()).isPresent()) {
            throw new IllegalStateException("Discount code already exists.");
        }
        DiscountCode code = new DiscountCode();
        code.setCode(request.code().toUpperCase());
        code.setDiscountType(request.discountType());
        code.setDiscountValue(request.discountValue());
        code.setMaxUses(request.maxUses());
        code.setIsPrivate(request.isPrivate());
        code.setValidUntil(request.validUntil());
        code.setCreatedBy(adminUser);
        String applicableTo = request.applicableTo() != null && !request.applicableTo().trim().isEmpty() 
            ? request.applicableTo().toUpperCase() 
            : "COURSES";
        code.setApplicableTo(applicableTo);
        if (("COURSES".equals(applicableTo) || "BOTH".equals(applicableTo)) 
            && request.applicableCourseIds() != null && !request.applicableCourseIds().isEmpty()) {
            Set<Course> courses = courseRepository.findAllById(request.applicableCourseIds())
                    .stream().collect(Collectors.toSet());
            code.setApplicableCourses(courses);
        }
        if (("PRODUCTS".equals(applicableTo) || "BOTH".equals(applicableTo)) 
            && request.applicableProductIds() != null && !request.applicableProductIds().isEmpty()) {
            Set<com.loyalixa.backend.shop.Product> products = productRepository.findAllById(request.applicableProductIds())
                    .stream().collect(Collectors.toSet());
            code.setApplicableProducts(products);
        }
        DiscountCode savedCode = discountRepository.save(code);
        if (Boolean.TRUE.equals(request.isPrivate()) && request.eligibleUserIds() != null && !request.eligibleUserIds().isEmpty()) {
            Set<UUID> uniqueUserIds = new HashSet<>(request.eligibleUserIds());
            List<User> eligibleUsers = userRepository.findAllById(uniqueUserIds);
            for (User user : eligibleUsers) {
                UserDiscount userDiscount = new UserDiscount();
                userDiscount.setUser(user);
                userDiscount.setDiscountCode(savedCode);
                userDiscount.setIsUsed(false);
                userDiscountRepository.save(userDiscount);
            }
        }
        return savedCode;
    }
    @Transactional(readOnly = true, noRollbackFor = {org.springframework.orm.jpa.JpaObjectRetrievalFailureException.class, jakarta.persistence.EntityNotFoundException.class})
    public List<DiscountResponse> getAllDiscountCodes() {
        List<UUID> validIds = discountRepository.findIdsWithValidUsers();
        if (validIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<DiscountResponse> responses = new ArrayList<>();
        for (UUID id : validIds) {
            try {
                DiscountCode codeWithRelations = discountRepository.findByIdWithRelations(id)
                        .orElse(null);
                if (codeWithRelations != null) {
                    responses.add(mapToDiscountResponse(codeWithRelations));
                }
            } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException | jakarta.persistence.EntityNotFoundException e) {
                continue;
            } catch (Exception e) {
                System.err.println("[DiscountService] Error loading discount code " + id + ": " + e.getMessage());
                continue;
            }
        }
        return responses;
    }
    @Transactional(readOnly = true, noRollbackFor = {org.springframework.orm.jpa.JpaObjectRetrievalFailureException.class, jakarta.persistence.EntityNotFoundException.class})
    public List<DiscountResponse> searchDiscountCodes(DiscountSearchRequest request) {
        Specification<DiscountCode> spec = buildSearchSpecification(request);
        List<DiscountCode> codes = discountRepository.findAll(spec);
        List<DiscountResponse> responses = new ArrayList<>();
        for (DiscountCode code : codes) {
            try {
                DiscountCode codeWithRelations = discountRepository.findByIdWithRelations(code.getId())
                        .orElse(code);
                responses.add(mapToDiscountResponse(codeWithRelations));
            } catch (org.springframework.orm.jpa.JpaObjectRetrievalFailureException | jakarta.persistence.EntityNotFoundException e) {
                continue;
            } catch (Exception e) {
                System.err.println("[DiscountService] Error loading discount code " + code.getId() + ": " + e.getMessage());
                continue;
            }
        }
        return responses;
    }
    private Specification<DiscountCode> buildSearchSpecification(DiscountSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (request.code() != null && !request.code().trim().isEmpty()) {
                String codePattern = "%" + request.code().trim().toUpperCase() + "%";
                predicates.add(cb.like(cb.upper(root.get("code")), codePattern));
            }
            if (request.discountType() != null && !request.discountType().trim().isEmpty()) {
                predicates.add(cb.equal(root.get("discountType"), request.discountType().trim()));
            }
            if (request.isPrivate() != null) {
                predicates.add(cb.equal(root.get("isPrivate"), request.isPrivate()));
            }
            LocalDateTime now = LocalDateTime.now();
            if (request.isExpired() != null) {
                if (request.isExpired()) {
                    predicates.add(cb.and(
                        cb.isNotNull(root.get("validUntil")),
                        cb.lessThan(root.get("validUntil"), now)
                    ));
                } else {
                    predicates.add(cb.or(
                        cb.isNull(root.get("validUntil")),
                        cb.greaterThanOrEqualTo(root.get("validUntil"), now)
                    ));
                }
            }
            if (request.isExhausted() != null) {
                if (request.isExhausted()) {
                    predicates.add(cb.and(
                        cb.isNotNull(root.get("maxUses")),
                        cb.greaterThanOrEqualTo(root.get("currentUses"), root.get("maxUses"))
                    ));
                } else {
                    predicates.add(cb.or(
                        cb.isNull(root.get("maxUses")),
                        cb.lessThan(root.get("currentUses"), root.get("maxUses"))
                    ));
                }
            }
            if (request.createdAtFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.createdAtFrom()));
            }
            if (request.createdAtTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.createdAtTo()));
            }
            if (request.validUntilFrom() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("validUntil"), request.validUntilFrom()));
            }
            if (request.validUntilTo() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("validUntil"), request.validUntilTo()));
            }
            if (query != null) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    public DiscountResponse mapToDiscountResponse(DiscountCode code) {
        List<DiscountResponse.CourseInfo> courses;
        if (code.getApplicableCourses() != null && !code.getApplicableCourses().isEmpty()) {
            courses = code.getApplicableCourses().stream()
                .map(c -> new DiscountResponse.CourseInfo(c.getId(), c.getTitle()))
                .collect(Collectors.toList());
        } else {
            courses = List.of();  
        }
        List<DiscountResponse.ProductInfo> products;
        if (code.getApplicableProducts() != null && !code.getApplicableProducts().isEmpty()) {
            products = code.getApplicableProducts().stream()
                .map(p -> new DiscountResponse.ProductInfo(p.getId(), p.getName()))
                .collect(Collectors.toList());
        } else {
            products = List.of();  
        }
        DiscountResponse.UserInfo createdByInfo = null;
        if (code.getCreatedBy() != null) {
            createdByInfo = new DiscountResponse.UserInfo(
                code.getCreatedBy().getId(),
                code.getCreatedBy().getEmail(),
                code.getCreatedBy().getUsername()
            );
        }
        DiscountResponse.UserInfo updatedByInfo = null;
        if (code.getUpdatedBy() != null) {
            updatedByInfo = new DiscountResponse.UserInfo(
                code.getUpdatedBy().getId(),
                code.getUpdatedBy().getEmail(),
                code.getUpdatedBy().getUsername()
            );
        }
        return new DiscountResponse(
            code.getId(),
            code.getCode(),
            code.getDiscountType(),
            code.getDiscountValue(),
            code.getMaxUses(),
            code.getCurrentUses(),
            code.getIsPrivate(),
            code.getValidUntil(),
            code.getCreatedAt(),
            code.getApplicableTo() != null ? code.getApplicableTo() : "COURSES",
            courses,
            products,
            createdByInfo,
            updatedByInfo
        );
    }
    @Transactional(readOnly = true)
    public DiscountCode getDiscountCodeById(UUID id) {
         return discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount code not found."));
    }
    @Transactional(readOnly = true)
    public DiscountCode getDiscountCodeByIdWithRelations(UUID id) {
        return discountRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount code not found."));
    }
    @Transactional(readOnly = true)
    public DiscountDetailsResponse getDiscountCodeDetails(UUID discountId) {
        DiscountCode code = discountRepository.findByIdWithRelations(discountId)
                .orElseThrow(() -> new IllegalArgumentException("Discount code not found."));
        Integer remainingUses = null;
        if (code.getMaxUses() != null) {
            remainingUses = Math.max(0, code.getMaxUses() - (code.getCurrentUses() != null ? code.getCurrentUses() : 0));
        }
        Boolean isExpired = false;
        Long daysUntilExpiry = null;
        Long hoursUntilExpiry = null;
        Long minutesUntilExpiry = null;
        if (code.getValidUntil() != null) {
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(now, code.getValidUntil());
            if (duration.isNegative() || duration.isZero()) {
                isExpired = true;
            } else {
                daysUntilExpiry = duration.toDays();
                hoursUntilExpiry = duration.toHours();
                minutesUntilExpiry = duration.toMinutes();
            }
        }
        List<DiscountDetailsResponse.CourseInfo> courseInfos = new ArrayList<>();
        if (code.getApplicableCourses() != null && !code.getApplicableCourses().isEmpty()) {
            courseInfos = code.getApplicableCourses().stream()
                    .map(c -> new DiscountDetailsResponse.CourseInfo(c.getId(), c.getTitle()))
                    .collect(Collectors.toList());
        }
        List<DiscountDetailsResponse.ProductInfo> productInfos = new ArrayList<>();
        if (code.getApplicableProducts() != null && !code.getApplicableProducts().isEmpty()) {
            productInfos = code.getApplicableProducts().stream()
                    .map(p -> new DiscountDetailsResponse.ProductInfo(p.getId(), p.getName()))
                    .collect(Collectors.toList());
        }
        List<DiscountDetailsResponse.UserInfo> eligibleUsers = new ArrayList<>();
        List<DiscountDetailsResponse.UserUsageInfo> usersWhoUsed = new ArrayList<>();
        if (Boolean.TRUE.equals(code.getIsPrivate())) {
            List<UserDiscount> userDiscounts = userDiscountRepository.findByDiscountCode(code);
            for (UserDiscount ud : userDiscounts) {
                User user = ud.getUser();
                DiscountDetailsResponse.UserInfo userInfo = new DiscountDetailsResponse.UserInfo(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getRole() != null ? user.getRole().getName() : null
                );
                eligibleUsers.add(userInfo);
                if (Boolean.TRUE.equals(ud.getIsUsed())) {
                    usersWhoUsed.add(new DiscountDetailsResponse.UserUsageInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getUsername(),
                        true,
                        null  
                    ));
                }
            }
        }
        DiscountDetailsResponse.UserInfo createdByInfo = null;
        if (code.getCreatedBy() != null) {
            User createdBy = code.getCreatedBy();
            createdByInfo = new DiscountDetailsResponse.UserInfo(
                createdBy.getId(),
                createdBy.getEmail(),
                createdBy.getUsername(),
                createdBy.getRole() != null ? createdBy.getRole().getName() : null
            );
        }
        DiscountDetailsResponse.UserInfo updatedByInfo = null;
        if (code.getUpdatedBy() != null) {
            User updatedBy = code.getUpdatedBy();
            updatedByInfo = new DiscountDetailsResponse.UserInfo(
                updatedBy.getId(),
                updatedBy.getEmail(),
                updatedBy.getUsername(),
                updatedBy.getRole() != null ? updatedBy.getRole().getName() : null
            );
        }
        return new DiscountDetailsResponse(
            code.getId(),
            code.getCode(),
            code.getDiscountType(),
            code.getDiscountValue(),
            code.getMaxUses(),
            code.getCurrentUses(),
            remainingUses,
            code.getIsPrivate(),
            code.getValidUntil(),
            code.getCreatedAt(),
            isExpired,
            daysUntilExpiry,
            hoursUntilExpiry,
            minutesUntilExpiry,
            code.getApplicableTo() != null ? code.getApplicableTo() : "COURSES",
            courseInfos,
            productInfos,
            eligibleUsers,
            usersWhoUsed,
            createdByInfo,
            updatedByInfo,
            null  
        );
    }
    @Transactional(readOnly = true)
    public List<com.loyalixa.backend.user.dto.UserAdminResponse> getEligibleUsers(UUID discountId) {
        if (!discountRepository.existsById(discountId)) {
            throw new IllegalArgumentException("Discount code not found.");
        }
        DiscountCode code = discountRepository.findById(discountId).orElse(null);
        if (code == null) {
            return List.of();
        }
        List<UserDiscount> userDiscounts = userDiscountRepository.findByDiscountCode(code);
        return userDiscounts.stream()
                .map(ud -> {
                    User user = ud.getUser();
                    String currentPlanName = null;
                    String currentPlanCode = null;
                    Optional<com.loyalixa.backend.subscription.UserSubscription> activeSubscription = 
                        userSubscriptionRepository.findActiveSubscriptionByUserId(user.getId(), LocalDateTime.now());
                    if (activeSubscription.isPresent()) {
                        currentPlanName = activeSubscription.get().getPlan().getName();
                        currentPlanCode = activeSubscription.get().getPlan().getCode();
                    } else {
                        currentPlanName = "Free Plan";
                        currentPlanCode = "FREE";
                    }
                    String actualUsername = user.getActualUsername();
                    return new com.loyalixa.backend.user.dto.UserAdminResponse(
                        user.getId(),
                        actualUsername != null ? actualUsername : user.getEmail(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().getName() : null,
                        user.getStatus(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.getLastLoginAt(),
                        user.getPasswordChangedAt(),
                        user.getDeletedAt(),
                        user.getDeletionReason(),
                        user.getCanAccessDashboard() != null ? user.getCanAccessDashboard() : false,
                        currentPlanName,
                        currentPlanCode
                    );
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public DiscountCode updateDiscountCode(UUID id, DiscountRequest request, User adminUser) {
        DiscountCode code = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount code not found."));
        LocalDateTime now = LocalDateTime.now();
        boolean isCurrentlyExpired = code.getValidUntil() != null && 
                                     (code.getValidUntil().isBefore(now) || code.getValidUntil().isEqual(now));
        if (isCurrentlyExpired) {
            if (request.validUntil() == null || request.validUntil().isBefore(now) || request.validUntil().isEqual(now)) {
                throw new IllegalStateException("Cannot update expired discount code. Please update the 'Valid Until' date to a future date to reactivate it, or delete it and create a new one.");
            }
        }
        if (discountRepository.findByCodeIgnoreCase(request.code())
                .filter(c -> !c.getId().equals(id))
                .isPresent()) {
            throw new IllegalStateException("Another discount code with this name already exists.");
        }
        code.setCode(request.code().toUpperCase());
        code.setDiscountType(request.discountType());
        code.setDiscountValue(request.discountValue());
        code.setMaxUses(request.maxUses());
        code.setIsPrivate(request.isPrivate());
        code.setValidUntil(request.validUntil());
        code.setUpdatedBy(adminUser);
        String applicableTo = request.applicableTo() != null && !request.applicableTo().trim().isEmpty() 
            ? request.applicableTo().toUpperCase() 
            : (code.getApplicableTo() != null ? code.getApplicableTo() : "COURSES");
        code.setApplicableTo(applicableTo);
        if ("COURSES".equals(applicableTo) || "BOTH".equals(applicableTo)) {
            if (request.applicableCourseIds() != null) {
                Set<Course> courses = courseRepository.findAllById(request.applicableCourseIds())
                        .stream().collect(Collectors.toSet());
                code.setApplicableCourses(courses);
            } else {
                if (code.getApplicableCourses() != null) {
                    code.getApplicableCourses().clear();
                }
            }
        } else {
            if (code.getApplicableCourses() != null) {
                code.getApplicableCourses().clear();
            }
        }
        if ("PRODUCTS".equals(applicableTo) || "BOTH".equals(applicableTo)) {
            if (request.applicableProductIds() != null) {
                Set<com.loyalixa.backend.shop.Product> products = productRepository.findAllById(request.applicableProductIds())
                        .stream().collect(Collectors.toSet());
                code.setApplicableProducts(products);
            } else {
                if (code.getApplicableProducts() != null) {
                    code.getApplicableProducts().clear();
                }
            }
        } else {
            if (code.getApplicableProducts() != null) {
                code.getApplicableProducts().clear();
            }
        }
        DiscountCode savedCode = discountRepository.save(code);
        userDiscountRepository.deleteByDiscountCode(savedCode);
        if (Boolean.TRUE.equals(request.isPrivate()) && request.eligibleUserIds() != null && !request.eligibleUserIds().isEmpty()) {
            Set<UUID> uniqueUserIds = new HashSet<>(request.eligibleUserIds());
            List<User> eligibleUsers = userRepository.findAllById(uniqueUserIds);
            for (User user : eligibleUsers) {
                UserDiscount userDiscount = new UserDiscount();
                userDiscount.setUser(user);
                userDiscount.setDiscountCode(savedCode);
                userDiscount.setIsUsed(false);
                userDiscountRepository.save(userDiscount);
            }
        }
        return savedCode;
    }
    @Transactional
    public void deleteDiscountCode(UUID id) {
        DiscountCode code = discountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Discount code not found."));
        List<UserDiscount> userDiscounts = userDiscountRepository.findByDiscountCode(code);
        if (!userDiscounts.isEmpty()) {
            long userCount = userDiscounts.size();
            throw new IllegalStateException("Cannot delete discount code. There are " + userCount + 
                    " user(s) associated with this discount code. Please remove all user associations first.");
        }
        discountRepository.delete(code);
    }
}