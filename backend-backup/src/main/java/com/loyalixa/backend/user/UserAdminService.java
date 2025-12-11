package com.loyalixa.backend.user;
import com.loyalixa.backend.security.RoleRepository;
import com.loyalixa.backend.security.Role;
import com.loyalixa.backend.user.dto.PageResponse;
import com.loyalixa.backend.user.dto.UserAdminRequest;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import com.loyalixa.backend.user.dto.UserSearchRequest;
import com.loyalixa.backend.user.dto.UserStatsResponse;
import com.loyalixa.backend.user.dto.UserFullDetailsResponse;
import com.loyalixa.backend.user.dto.UserFullDetailsUpdateRequest;
import com.loyalixa.backend.course.Enrollment;
import com.loyalixa.backend.course.EnrollmentRepository;
import com.loyalixa.backend.course.LessonProgress;
import com.loyalixa.backend.course.LessonProgressRepository;
import com.loyalixa.backend.course.LessonRepository;
import com.loyalixa.backend.discount.DiscountCode;
import com.loyalixa.backend.discount.DiscountCodeRepository;
import com.loyalixa.backend.discount.UserDiscount;
import com.loyalixa.backend.discount.UserDiscountRepository;
import com.loyalixa.backend.marketing.GiftVoucher;
import com.loyalixa.backend.marketing.GiftVoucherRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import com.loyalixa.backend.user.preferences.UserPreferenceService;
import com.loyalixa.backend.user.preferences.NotificationSettingService;
import com.loyalixa.backend.user.preferences.UserPreference;
import com.loyalixa.backend.user.preferences.UserNotificationSetting;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loyalixa.backend.user.preferences.UserPreferenceRepository;
import com.loyalixa.backend.user.preferences.UserNotificationSettingRepository;
import com.loyalixa.backend.security.RefreshTokenService;
import com.loyalixa.backend.subscription.UserSubscription;
import com.loyalixa.backend.subscription.UserSubscriptionRepository;
@Service
public class UserAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserDiscountRepository userDiscountRepository;
    private final DiscountCodeRepository discountCodeRepository;
    private final GiftVoucherRepository giftVoucherRepository;
    private final LessonRepository lessonRepository;
    private final UserProfileRepository userProfileRepository;
    private final EntityManager entityManager;
    private final UserPreferenceService userPreferenceService;
    private final NotificationSettingService notificationSettingService;
    private final AvatarService avatarService;
    private final RefreshTokenService refreshTokenService;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserNotificationSettingRepository userNotificationSettingRepository;
    private final UserSuspensionHistoryRepository suspensionHistoryRepository;
    private final UserBanHistoryRepository banHistoryRepository;
    private final com.loyalixa.backend.staff.StaffRepository staffRepository;  
    private final UserSubscriptionRepository userSubscriptionRepository;  
    public UserAdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            EnrollmentRepository enrollmentRepository,
            LessonProgressRepository lessonProgressRepository,
            UserDiscountRepository userDiscountRepository,
            DiscountCodeRepository discountCodeRepository,
            GiftVoucherRepository giftVoucherRepository,
            LessonRepository lessonRepository,
            UserProfileRepository userProfileRepository,
            EntityManager entityManager,
            UserPreferenceService userPreferenceService,
            NotificationSettingService notificationSettingService,
            AvatarService avatarService,
            RefreshTokenService refreshTokenService,
            UserPreferenceRepository userPreferenceRepository,
            UserNotificationSettingRepository userNotificationSettingRepository,
            UserSuspensionHistoryRepository suspensionHistoryRepository,
            UserBanHistoryRepository banHistoryRepository,
            com.loyalixa.backend.staff.StaffRepository staffRepository,  
            UserSubscriptionRepository userSubscriptionRepository  
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userDiscountRepository = userDiscountRepository;
        this.discountCodeRepository = discountCodeRepository;
        this.giftVoucherRepository = giftVoucherRepository;
        this.lessonRepository = lessonRepository;
        this.userProfileRepository = userProfileRepository;
        this.entityManager = entityManager;
        this.userPreferenceService = userPreferenceService;
        this.notificationSettingService = notificationSettingService;
        this.avatarService = avatarService;
        this.refreshTokenService = refreshTokenService;
        this.userPreferenceRepository = userPreferenceRepository;
        this.userNotificationSettingRepository = userNotificationSettingRepository;
        this.suspensionHistoryRepository = suspensionHistoryRepository;
        this.banHistoryRepository = banHistoryRepository;
        this.staffRepository = staffRepository;  
        this.userSubscriptionRepository = userSubscriptionRepository;  
    }
    @Transactional(readOnly = true)
    public List<UserAdminResponse> getAllUsers() {
        return userRepository.findAllUsersWithRolesAndPermissions().stream()
                .map(this::mapToUserAdminResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public PageResponse<UserAdminResponse> searchUsers(UserSearchRequest request) {
        Pageable pageable = PageRequest.of(request.page(), request.size());
        String search = (request.search() != null && !request.search().trim().isEmpty()) ? request.search().trim() : null;
        List<String> roles = (request.roles() != null && !request.roles().isEmpty()) ? request.roles() : null;
        List<String> statuses = (request.statuses() != null && !request.statuses().isEmpty()) ? request.statuses() : null;
        String username = (request.username() != null && !request.username().trim().isEmpty()) ? request.username().trim() : null;
        List<String> authProviders = (request.authProviders() != null && !request.authProviders().isEmpty()) ? request.authProviders() : null;
        List<String> deviceTypes = (request.deviceTypes() != null && !request.deviceTypes().isEmpty()) ? request.deviceTypes() : null;
        List<String> browsers = (request.browsers() != null && !request.browsers().isEmpty()) ? request.browsers() : null;
        List<String> operatingSystems = (request.operatingSystems() != null && !request.operatingSystems().isEmpty()) ? request.operatingSystems() : null;
        String ipAddress = (request.ipAddress() != null && !request.ipAddress().trim().isEmpty()) ? request.ipAddress().trim() : null;
        String appealStatus = (request.appealStatus() != null && !request.appealStatus().trim().isEmpty()) ? request.appealStatus().trim() : null;
        Boolean inverse = request.inverse() != null && request.inverse();
        Specification<User> spec = buildSearchSpecification(search, roles, statuses, username, authProviders,
                request.createdAtFrom(), request.createdAtTo(),
                request.lastLoginFrom(), request.lastLoginTo(),
                request.passwordChangedFrom(), request.passwordChangedTo(),
                request.suspendedFrom(), request.suspendedTo(),
                request.bannedFrom(), request.bannedTo(),
                deviceTypes, browsers, operatingSystems, ipAddress,
                request.maxDevices(), appealStatus, inverse);
        Page<User> page = userRepository.findAll(spec, pageable);
        List<UserAdminResponse> content = page.getContent().stream()
                .map(this::mapToUserAdminResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(
            content,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
    private Specification<User> buildSearchSpecification(
            String search, List<String> roles, List<String> statuses, String username, List<String> authProviders,
            LocalDateTime createdAtFrom, LocalDateTime createdAtTo,
            LocalDateTime lastLoginFrom, LocalDateTime lastLoginTo,
            LocalDateTime passwordChangedFrom, LocalDateTime passwordChangedTo,
            LocalDateTime suspendedFrom, LocalDateTime suspendedTo,
            LocalDateTime bannedFrom, LocalDateTime bannedTo,
            List<String> deviceTypes, List<String> browsers, List<String> operatingSystems, String ipAddress,
            Integer maxDevices, String appealStatus, Boolean inverse) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Object, Object> roleJoin = root.join("role", JoinType.INNER);
            if (search != null && !search.isEmpty()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                try {
                    UUID searchId = UUID.fromString(search.trim());
                    Predicate idPredicate = cb.equal(root.get("id"), searchId);
                    Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                    Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), searchPattern);
                    predicates.add(cb.or(idPredicate, emailPredicate, usernamePredicate));
                } catch (IllegalArgumentException e) {
                    Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchPattern);
                    Predicate usernamePredicate = cb.like(cb.lower(root.get("username")), searchPattern);
                    predicates.add(cb.or(emailPredicate, usernamePredicate));
                }
            }
            if (roles != null && !roles.isEmpty()) {
                predicates.add(roleJoin.get("name").in(roles));
            }
            if (statuses != null && !statuses.isEmpty()) {
                predicates.add(root.get("status").in(statuses));
            }
            if (username != null && !username.isEmpty()) {
                String usernamePattern = "%" + username.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("username")), usernamePattern));
            }
            if (authProviders != null && !authProviders.isEmpty()) {
                predicates.add(root.get("authProvider").in(authProviders));
            }
            if (createdAtFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdAtFrom));
            }
            if (createdAtTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), createdAtTo));
            }
            if (lastLoginFrom != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("lastLoginAt")),
                    cb.greaterThanOrEqualTo(root.get("lastLoginAt"), lastLoginFrom)
                ));
            }
            if (lastLoginTo != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("lastLoginAt")),
                    cb.lessThanOrEqualTo(root.get("lastLoginAt"), lastLoginTo)
                ));
            }
            if (passwordChangedFrom != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("passwordChangedAt")),
                    cb.greaterThanOrEqualTo(root.get("passwordChangedAt"), passwordChangedFrom)
                ));
            }
            if (passwordChangedTo != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("passwordChangedAt")),
                    cb.lessThanOrEqualTo(root.get("passwordChangedAt"), passwordChangedTo)
                ));
            }
            if (suspendedFrom != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("suspendedAt")),
                    cb.greaterThanOrEqualTo(root.get("suspendedAt"), suspendedFrom)
                ));
            }
            if (suspendedTo != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("suspendedAt")),
                    cb.lessThanOrEqualTo(root.get("suspendedAt"), suspendedTo)
                ));
            }
            if (bannedFrom != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("bannedAt")),
                    cb.greaterThanOrEqualTo(root.get("bannedAt"), bannedFrom)
                ));
            }
            if (bannedTo != null) {
                predicates.add(cb.or(
                    cb.isNull(root.get("bannedAt")),
                    cb.lessThanOrEqualTo(root.get("bannedAt"), bannedTo)
                ));
            }
            if (deviceTypes != null && !deviceTypes.isEmpty()) {
                predicates.add(root.get("deviceType").in(deviceTypes));
            }
            if (browsers != null && !browsers.isEmpty()) {
                List<Predicate> browserPredicates = new ArrayList<>();
                for (String browser : browsers) {
                    String browserPattern = "%" + browser.toLowerCase() + "%";
                    browserPredicates.add(cb.like(cb.lower(root.get("browser")), browserPattern));
                }
                predicates.add(cb.or(browserPredicates.toArray(new Predicate[0])));
            }
            if (operatingSystems != null && !operatingSystems.isEmpty()) {
                List<Predicate> osPredicates = new ArrayList<>();
                for (String os : operatingSystems) {
                    String osPattern = "%" + os.toLowerCase() + "%";
                    osPredicates.add(cb.like(cb.lower(root.get("operatingSystem")), osPattern));
                }
                predicates.add(cb.or(osPredicates.toArray(new Predicate[0])));
            }
            if (ipAddress != null && !ipAddress.isEmpty()) {
                String ipPattern = "%" + ipAddress.toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("ipAddress")), ipPattern));
            }
            if (maxDevices != null) {
                predicates.add(cb.equal(root.get("maxDevices"), maxDevices));
            }
            if (appealStatus != null && !appealStatus.isEmpty()) {
                if ("true".equalsIgnoreCase(appealStatus)) {
                    predicates.add(cb.equal(root.get("appealRequested"), true));
                } else if ("false".equalsIgnoreCase(appealStatus)) {
                    predicates.add(cb.or(
                        cb.isNull(root.get("appealRequested")),
                        cb.equal(root.get("appealRequested"), false)
                    ));
                } else {
                    predicates.add(cb.equal(root.get("appealStatus"), appealStatus));
                }
            }
            if (query != null) {
                query.orderBy(cb.desc(root.get("createdAt")));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            if (inverse != null && inverse) {
                Predicate combinedPredicate = cb.and(predicates.toArray(new Predicate[0]));
                return cb.not(combinedPredicate);
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    @Transactional
    public UserAdminResponse updateUserRoleAndStatus(UUID userId, UserAdminRequest request, UUID adminId) {
        User targetUser = userRepository.findByIdWithStaff(userId)
                .orElseGet(() -> userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User not found.")));
        if (request.newRoleName() != null) {
            Role newRole = roleRepository.findByName(request.newRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.newRoleName()));
            targetUser.setRole(newRole);
        }
        if (request.newStatus() != null) {
            String oldStatus = targetUser.getStatus();
            targetUser.setStatus(request.newStatus());
            LocalDateTime now = LocalDateTime.now();
            String reason = request.actionReason();
            User adminUser = userRepository.findById(adminId).orElse(null);
            String adminEmail = adminUser != null ? adminUser.getEmail() : "System";
            if (request.newStatus().equals("SUSPENDED")) {
                targetUser.setSuspendedAt(now);
                targetUser.setSuspendReason(reason);
                String suspensionType = (request.suspensionType() != null && !request.suspensionType().trim().isEmpty()) 
                    ? request.suspensionType().trim().toUpperCase() : "PERMANENT";
                targetUser.setSuspensionType(suspensionType);
                LocalDateTime suspendedUntil = null;
                if ("TEMPORARY".equals(suspensionType) && request.suspendedUntil() != null) {
                    suspendedUntil = request.suspendedUntil();
                    System.out.println("[UserAdminService] Setting suspendedUntil to: " + suspendedUntil + " (from request: " + request.suspendedUntil() + ")");
                    targetUser.setSuspendedUntil(suspendedUntil);
                } else {
                    targetUser.setSuspendedUntil(null);  
                }
                UserSuspensionHistory suspensionHistory = new UserSuspensionHistory();
                suspensionHistory.setUser(targetUser);
                suspensionHistory.setAction("SUSPEND");
                suspensionHistory.setReason(reason);
                suspensionHistory.setSuspensionType(suspensionType);
                suspensionHistory.setSuspendedUntil(suspendedUntil);
                suspensionHistory.setActionAt(now);
                suspensionHistory.setPerformedBy(adminUser);
                suspensionHistory.setPerformedByEmail(adminEmail);
                suspensionHistoryRepository.save(suspensionHistory);
                if ("BANNED".equals(oldStatus)) {
                    UserBanHistory banHistory = new UserBanHistory();
                    banHistory.setUser(targetUser);
                    banHistory.setAction("UNBAN");
                    banHistory.setReason("Account suspended, ban removed");
                    banHistory.setActionAt(now);
                    banHistory.setPerformedBy(adminUser);
                    banHistory.setPerformedByEmail(adminEmail);
                    banHistoryRepository.save(banHistory);
                    targetUser.setBannedAt(null);
                    targetUser.setBanReason(null);
                    targetUser.setBannedUntil(null);
                    targetUser.setBanType(null);
                }
                targetUser.setAppealRequested(false);
                targetUser.setAppealReason(null);
                targetUser.setAppealRequestedAt(null);
                targetUser.setAppealStatus(null);
                targetUser.setAppealReviewedAt(null);
                targetUser.setAppealReviewedBy(null);
                refreshTokenService.logoutUser(targetUser.getId());
            } else if (request.newStatus().equals("BANNED")) {
                targetUser.setBannedAt(now);
                targetUser.setBanReason(reason);
                String banType = (request.banType() != null && !request.banType().trim().isEmpty()) 
                    ? request.banType().trim().toUpperCase() : "PERMANENT";
                targetUser.setBanType(banType);
                LocalDateTime bannedUntil = null;
                if ("TEMPORARY".equals(banType) && request.bannedUntil() != null) {
                    bannedUntil = request.bannedUntil();
                    System.out.println("[UserAdminService] Setting bannedUntil to: " + bannedUntil + " (from request: " + request.bannedUntil() + ")");
                    targetUser.setBannedUntil(bannedUntil);
                } else {
                    targetUser.setBannedUntil(null);  
                }
                UserBanHistory banHistory = new UserBanHistory();
                banHistory.setUser(targetUser);
                banHistory.setAction("BAN");
                banHistory.setReason(reason);
                banHistory.setBanType(banType);
                banHistory.setBannedUntil(bannedUntil);
                banHistory.setActionAt(now);
                banHistory.setPerformedBy(adminUser);
                banHistory.setPerformedByEmail(adminEmail);
                banHistoryRepository.save(banHistory);
                if ("SUSPENDED".equals(oldStatus)) {
                    UserSuspensionHistory suspensionHistory = new UserSuspensionHistory();
                    suspensionHistory.setUser(targetUser);
                    suspensionHistory.setAction("UNSUSPEND");
                    suspensionHistory.setReason("Account banned, suspension removed");
                    suspensionHistory.setActionAt(now);
                    suspensionHistory.setPerformedBy(adminUser);
                    suspensionHistory.setPerformedByEmail(adminEmail);
                    suspensionHistoryRepository.save(suspensionHistory);
                    targetUser.setSuspendedAt(null);
                    targetUser.setSuspendReason(null);
                    targetUser.setSuspendedUntil(null);
                    targetUser.setSuspensionType(null);
                }
                targetUser.setAppealRequested(false);
                targetUser.setAppealReason(null);
                targetUser.setAppealRequestedAt(null);
                targetUser.setAppealStatus(null);
                targetUser.setAppealReviewedAt(null);
                targetUser.setAppealReviewedBy(null);
                refreshTokenService.logoutUser(targetUser.getId());
            } else if (request.newStatus().equals("ACTIVE")) {
                if ("SUSPENDED".equals(oldStatus)) {
                    UserSuspensionHistory suspensionHistory = new UserSuspensionHistory();
                    suspensionHistory.setUser(targetUser);
                    suspensionHistory.setAction("REACTIVATE");
                    suspensionHistory.setReason(reason != null ? reason : "Account reactivated");
                    suspensionHistory.setActionAt(now);
                    suspensionHistory.setPerformedBy(adminUser);
                    suspensionHistory.setPerformedByEmail(adminEmail);
                    suspensionHistoryRepository.save(suspensionHistory);
                } else if ("BANNED".equals(oldStatus)) {
                    UserBanHistory banHistory = new UserBanHistory();
                    banHistory.setUser(targetUser);
                    banHistory.setAction("REACTIVATE");
                    banHistory.setReason(reason != null ? reason : "Account reactivated");
                    banHistory.setActionAt(now);
                    banHistory.setPerformedBy(adminUser);
                    banHistory.setPerformedByEmail(adminEmail);
                    banHistoryRepository.save(banHistory);
                }
                targetUser.setSuspendedUntil(null);
                targetUser.setBannedUntil(null);
                targetUser.setAppealRequested(false);
                targetUser.setAppealReason(null);
                targetUser.setAppealRequestedAt(null);
                targetUser.setAppealStatus(null);
                targetUser.setAppealReviewedAt(null);
                targetUser.setAppealReviewedBy(null);
            } else if (request.newStatus().equals("DELETED")) {
                targetUser.setDeletedAt(LocalDateTime.now());
                targetUser.setDeletionReason(reason);
                refreshTokenService.logoutUser(targetUser.getId());
            } else if (request.newStatus().equals("BLOCKED")) {
                refreshTokenService.logoutUser(targetUser.getId());
            }
        }
        if (request.canAccessDashboard() != null) {
            if (targetUser.getRole() != null && !"STUDENT".equalsIgnoreCase(targetUser.getRole().getName())) {
                com.loyalixa.backend.staff.Staff staff = staffRepository.findByUserId(targetUser.getId())
                        .orElseGet(() -> {
                            com.loyalixa.backend.staff.Staff newStaff = new com.loyalixa.backend.staff.Staff();
                            newStaff.setUser(targetUser);
                            return newStaff;
                        });
                staff.setCanAccessDashboard(request.canAccessDashboard());
                staffRepository.save(staff);
            }
        }
        User savedUser = userRepository.save(targetUser);
        return mapToUserAdminResponse(savedUser);
    }
    @Transactional
    public void deleteUserPermanently(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        refreshTokenService.logoutUser(userId);
        if (user.getUserProfile() != null) {
            userProfileRepository.delete(user.getUserProfile());
        }
        userPreferenceRepository.findByUserId(userId).ifPresent(userPreferenceRepository::delete);
        userNotificationSettingRepository.deleteByUserId(userId);
        userRepository.delete(user);
    }
    @Transactional(readOnly = true)
    public UserAdminResponse getUserDetails(UUID userId) {  
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return mapToUserAdminResponse(user);
    }
    @Transactional(readOnly = true)
    public List<UserAdminResponse> findUsersByMultipleIdentifiers(List<String> identifiers) {
        List<UserAdminResponse> results = new ArrayList<>();
        List<UUID> foundIds = new ArrayList<>();  
        for (String identifier : identifiers) {
            if (identifier == null || identifier.trim().isEmpty()) {
                continue;
            }
            String trimmed = identifier.trim();
            try {
                UUID userId = UUID.fromString(trimmed);
                User user = userRepository.findById(userId).orElse(null);
                if (user != null && !foundIds.contains(userId)) {
                    results.add(mapToUserAdminResponse(user));
                    foundIds.add(userId);
                }
                continue;  
            } catch (IllegalArgumentException e) {
            }
            User userByUsername = userRepository.findByUsername(trimmed).orElse(null);
            if (userByUsername != null && !foundIds.contains(userByUsername.getId())) {
                results.add(mapToUserAdminResponse(userByUsername));
                foundIds.add(userByUsername.getId());
                continue;
            }
            User userByEmail = userRepository.findByEmail(trimmed).orElse(null);
            if (userByEmail != null && !foundIds.contains(userByEmail.getId())) {
                results.add(mapToUserAdminResponse(userByEmail));
                foundIds.add(userByEmail.getId());
            }
        }
        return results;
    }
    public UserAdminResponse mapToUserAdminResponse(User user) {
        String actualUsername = user.getActualUsername();
        String currentPlanName = null;
        String currentPlanCode = null;
        Optional<UserSubscription> activeSubscription = userSubscriptionRepository.findActiveSubscriptionByUserId(user.getId(), LocalDateTime.now());
        if (activeSubscription.isPresent()) {
            currentPlanName = activeSubscription.get().getPlan().getName();
            currentPlanCode = activeSubscription.get().getPlan().getCode();
        } else {
            currentPlanName = "Free Plan";
            currentPlanCode = "FREE";
        }
        return new UserAdminResponse(
            user.getId(),
            actualUsername != null ? actualUsername : user.getEmail(),
            user.getEmail(),
            user.getRole().getName(),  
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
    }
    @Transactional(readOnly = true)
    public long getActiveUserCount() {
        long activeCount = userRepository.countByStatus("ACTIVE");
        return activeCount;
    }
    @Transactional(readOnly = true)
    public long getTotalUserCount() {
        return userRepository.count();
    }
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        long total = userRepository.count();
        long active = userRepository.countByStatus("ACTIVE");
        long suspended = userRepository.countByStatus("SUSPENDED");
        long banned = userRepository.countByStatus("BANNED");
        List<UserStatsResponse.RoleStats> roleStats = roleRepository.findAll().stream()
                .map(role -> {
                    long count = userRepository.countByRoleName(role.getName());
                    return new UserStatsResponse.RoleStats(role.getName(), count);
                })
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();
        long totalSubscriptions = userSubscriptionRepository.count();
        long activeSubscriptions = userSubscriptionRepository.findAllActiveSubscriptions(now).size();
        long expiredSubscriptions = userSubscriptionRepository.findExpiredSubscriptions(now).size();
        long cancelledSubscriptions = userSubscriptionRepository.findAll().stream()
                .filter(s -> "CANCELLED".equals(s.getStatus()))
                .count();
        long freePlanSubscriptions = userSubscriptionRepository.findAll().stream()
                .filter(s -> s.getPlan() != null && "FREE".equalsIgnoreCase(s.getPlan().getCode()))
                .count();
        return new UserStatsResponse(
            total, active, suspended, banned, roleStats,
            totalSubscriptions, activeSubscriptions, expiredSubscriptions, cancelledSubscriptions, freePlanSubscriptions
        );
    }
    @Transactional(readOnly = true)
    public UserFullDetailsResponse getUserFullDetails(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        UserProfile profile = user.getUserProfile();
        UserFullDetailsResponse.AccountInfo accountInfo = new UserFullDetailsResponse.AccountInfo(
            user.getId(),
            user.getActualUsername(),
            user.getEmail(),
            user.getStatus(),
            user.getAuthProvider(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            user.getLastLoginAt(),
            user.getPasswordChangedAt(),
            user.getDeletedAt(),
            user.getDeletionReason(),
            user.getSuspendedAt(),
            user.getSuspendReason(),
            user.getSuspendedUntil(),
            user.getSuspensionType(),
            user.getBannedAt(),
            user.getBanReason(),
            user.getBannedUntil(),
            user.getBanType(),
            user.getAppealRequested() != null ? user.getAppealRequested() : false,
            user.getAppealReason(),
            user.getAppealRequestedAt(),
            user.getAppealStatus(),
            user.getAppealReviewedAt(),
            user.getAppealReviewedBy() != null ? user.getAppealReviewedBy().getId() : null,
            user.getAppealReviewedBy() != null ? user.getAppealReviewedBy().getEmail() : null
        );
        UserFullDetailsResponse.ProfileInfo profileInfo = profile != null ?
            new UserFullDetailsResponse.ProfileInfo(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getBio(),
                avatarService.getAvatarUrl(profile.getAvatarUrl(), userId),  
                profile.getPhoneNumber(),
                profile.getPhoneSocialApp(),
                profile.getSecondaryEmail(),
                profile.getTshirtSize(),
                profile.getExtraInfo()
            ) : new UserFullDetailsResponse.ProfileInfo(null, null, null, avatarService.getRandomAvatar(userId), null, null, null, null, null);
        Role role = user.getRole();
        List<String> permissions = role.getPermissions().stream()
                .map(p -> p.getName())
                .collect(Collectors.toList());
        UserFullDetailsResponse.RoleInfo roleInfo = new UserFullDetailsResponse.RoleInfo(
            role.getName(),
            permissions
        );
        List<Enrollment> enrollments = enrollmentRepository.findByStudentIdWithCourse(userId);
        List<UserFullDetailsResponse.CourseDetails> courseDetails = new ArrayList<>();
        long totalEnrollments = enrollments.size();
        long activeEnrollments = enrollments.stream().filter(e -> "ACTIVE".equals(e.getEnrollmentStatus())).count();
        long completedEnrollments = enrollments.stream().filter(e -> "COMPLETED".equals(e.getEnrollmentStatus())).count();
        long freeEnrollments = enrollments.stream().filter(e -> "FREE".equals(e.getPaymentStatus())).count();
        long paidEnrollments = enrollments.stream().filter(e -> "PAID".equals(e.getPaymentStatus())).count();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        List<LessonProgress> allLessonProgresses = lessonProgressRepository.findByStudentIdWithCourse(userId);
        for (Enrollment enrollment : enrollments) {
            List<LessonProgress> courseProgresses = allLessonProgresses.stream()
                    .filter(lp -> {
                        try {
                            return lp.getLesson().getSection().getCourse().getId().equals(enrollment.getCourse().getId());
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .collect(Collectors.toList());
            long totalLessons = lessonRepository.countByCourseId(enrollment.getCourse().getId());
            long completedLessons = courseProgresses.stream().filter(LessonProgress::getIsCompleted).count();
            int progressPercentage = totalLessons > 0 ? (int)((completedLessons * 100) / totalLessons) : 0;
            long totalTimeSpentSeconds = courseProgresses.stream()
                    .mapToLong(LessonProgress::getTimeSpentSeconds)
                    .sum();
            courseDetails.add(new UserFullDetailsResponse.CourseDetails(
                enrollment.getCourse().getId(),
                enrollment.getCourse().getTitle(),
                enrollment.getEnrollmentDate() != null ? enrollment.getEnrollmentDate().format(formatter) : null,
                enrollment.getPaymentStatus(),
                enrollment.getEnrollmentSource(),
                enrollment.getEnrollmentStatus(),
                enrollment.getStartDate() != null ? enrollment.getStartDate().format(formatter) : null,
                totalLessons,
                completedLessons,
                progressPercentage,
                totalTimeSpentSeconds
            ));
        }
        UserFullDetailsResponse.CoursesInfo coursesInfo = new UserFullDetailsResponse.CoursesInfo(
            totalEnrollments,
            activeEnrollments,
            completedEnrollments,
            freeEnrollments,
            paidEnrollments,
            courseDetails
        );
        List<UserDiscount> userDiscounts = userDiscountRepository.findByUserId(userId);
        long totalAssignedCodes = userDiscounts.size();
        long usedCodes = userDiscountRepository.countUsedByUserId(userId);
        long unusedCodes = userDiscountRepository.countUnusedByUserId(userId);
        List<UserFullDetailsResponse.UserDiscountDetails> assignedDiscounts = userDiscounts.stream()
                .map(ud -> new UserFullDetailsResponse.UserDiscountDetails(
                    ud.getDiscountCode().getId(),
                    ud.getDiscountCode().getCode(),
                    ud.getDiscountCode().getDiscountType(),
                    ud.getDiscountCode().getDiscountValue(),
                    ud.getIsUsed(),
                    ud.getDiscountCode().getCreatedAt()
                ))
                .collect(Collectors.toList());
        List<DiscountCode> codesCreated = discountCodeRepository.findByCreatedByUserId(userId);
        long codesIssuedByUser = codesCreated.size();
        List<UserFullDetailsResponse.DiscountCodeCreated> codesCreatedList = codesCreated.stream()
                .map(dc -> new UserFullDetailsResponse.DiscountCodeCreated(
                    dc.getId(),
                    dc.getCode(),
                    dc.getDiscountType(),
                    dc.getDiscountValue(),
                    dc.getMaxUses(),
                    dc.getCurrentUses(),
                    dc.getCreatedAt()
                ))
                .collect(Collectors.toList());
        UserFullDetailsResponse.DiscountsInfo discountsInfo = new UserFullDetailsResponse.DiscountsInfo(
            totalAssignedCodes,
            usedCodes,
            unusedCodes,
            codesIssuedByUser,
            totalAssignedCodes,
            assignedDiscounts,
            codesCreatedList
        );
        List<GiftVoucher> giftsSent = giftVoucherRepository.findBySenderId(userId);
        long totalGiftsSent = giftsSent.size();
        long redeemedGiftsSent = giftsSent.stream().filter(gv -> "REDEEMED".equals(gv.getStatus())).count();
        long pendingGiftsSent = totalGiftsSent - redeemedGiftsSent;
        List<UserFullDetailsResponse.GiftSent> giftsSentList = giftsSent.stream()
                .map(gv -> new UserFullDetailsResponse.GiftSent(
                    gv.getId(),
                    gv.getCourse().getId(),
                    gv.getCourse().getTitle(),
                    gv.getRecipientEmail(),
                    gv.getVoucherCode(),
                    gv.getStatus(),
                    gv.getIssuedAt(),
                    gv.getRedeemedAt()
                ))
                .collect(Collectors.toList());
        List<GiftVoucher> giftsReceived = giftVoucherRepository.findByRedeemerId(userId);
        List<GiftVoucher> giftsByEmail = giftVoucherRepository.findByRecipientEmail(user.getEmail());
        Set<UUID> giftIds = new java.util.HashSet<>();
        List<GiftVoucher> allGiftsReceived = new ArrayList<>();
        for (GiftVoucher gv : giftsReceived) {
            if (!giftIds.contains(gv.getId())) {
                allGiftsReceived.add(gv);
                giftIds.add(gv.getId());
            }
        }
        for (GiftVoucher gv : giftsByEmail) {
            if (!giftIds.contains(gv.getId())) {
                allGiftsReceived.add(gv);
                giftIds.add(gv.getId());
            }
        }
        long totalGiftsReceived = allGiftsReceived.size();
        long redeemedGiftsReceived = allGiftsReceived.stream().filter(gv -> "REDEEMED".equals(gv.getStatus())).count();
        List<UserFullDetailsResponse.GiftReceived> giftsReceivedList = allGiftsReceived.stream()
                .map(gv -> new UserFullDetailsResponse.GiftReceived(
                    gv.getId(),
                    gv.getCourse().getId(),
                    gv.getCourse().getTitle(),
                    gv.getSender() != null ? gv.getSender().getEmail() : null,
                    gv.getVoucherCode(),
                    gv.getIssuedAt(),
                    gv.getRedeemedAt()
                ))
                .collect(Collectors.toList());
        UserFullDetailsResponse.GiftsInfo giftsInfo = new UserFullDetailsResponse.GiftsInfo(
            totalGiftsSent,
            redeemedGiftsSent,
            pendingGiftsSent,
            totalGiftsReceived,
            redeemedGiftsReceived,
            giftsSentList,
            giftsReceivedList
        );
        List<LessonProgress> allProgress = lessonProgressRepository.findByStudentId(userId);
        long totalLessonsCompleted = lessonProgressRepository.countCompletedLessonsByStudentId(userId);
        long totalLessonsInProgress = allProgress.stream().filter(lp -> !lp.getIsCompleted()).count();
        Long totalTimeSpentSecondsSum = lessonProgressRepository.sumTimeSpentByStudentId(userId);
        long totalTimeSpent = totalTimeSpentSecondsSum != null ? totalTimeSpentSecondsSum : 0L;
        long totalCoursesWithProgress = enrollments.stream()
                .filter(e -> {
                    return allProgress.stream().anyMatch(lp -> {
                        try {
                            return lp.getLesson().getSection().getCourse().getId().equals(e.getCourse().getId());
                        } catch (Exception ex) {
                            return false;
                        }
                    });
                })
                .count();
        UserFullDetailsResponse.ProgressInfo progressInfo = new UserFullDetailsResponse.ProgressInfo(
            totalLessonsCompleted,
            totalLessonsInProgress,
            totalTimeSpent,
            totalCoursesWithProgress
        );
        UserFullDetailsResponse.Statistics statistics = new UserFullDetailsResponse.Statistics(
            totalEnrollments,
            activeEnrollments,
            completedEnrollments,
            totalAssignedCodes,
            usedCodes,
            unusedCodes,
            totalGiftsSent,
            totalGiftsReceived,
            codesIssuedByUser,
            totalLessonsCompleted,
            totalTimeSpent / 3600  
        );
        UserPreference pref = userPreferenceService.getOrCreateDefaults(userId);
        java.util.List<UserNotificationSetting> notifList = notificationSettingService.listForUser(userId);
        java.util.Map<String, Boolean> notifMap;
        if (notifList.isEmpty()) {
            notifMap = new java.util.HashMap<>();
            notifMap.put("MARKETING_PROMO", true);
            notifMap.put("SECURITY_ALERTS", true);
            notifMap.put("FOLLOWED_INSTRUCTOR_NEW_COURSE", true);
            notifMap.put("FOLLOWED_INSTRUCTOR_LIVE_SESSION", true);
            notifMap.put("COURSE_REMINDERS", true);
            notifMap.put("WEEKLY_SUMMARY", true);
        } else {
            notifMap = notifList.stream()
                    .collect(java.util.stream.Collectors.toMap(UserNotificationSetting::getPreferenceType, UserNotificationSetting::isEnabled));
        }
        UserFullDetailsResponse.PreferencesInfo preferencesInfo = new UserFullDetailsResponse.PreferencesInfo(
            pref.getUiTheme(),
            pref.getUiLanguage(),
            pref.getTimezone(),
            notifMap
        );
        UserFullDetailsResponse.DeviceMetadataInfo deviceMetadata = new UserFullDetailsResponse.DeviceMetadataInfo(
            user.getUserAgent(),
            user.getBrowser(),
            user.getOperatingSystem(),
            user.getDeviceType(),
            user.getIpAddress(),
            user.getAcceptLanguage(),
            user.getAcceptEncoding(),
            user.getDnt(),
            user.getReferrer(),
            user.getHost(),
            user.getOrigin(),
            user.getTimezone(),
            user.getPlatform(),
            user.getScreenWidth(),
            user.getScreenHeight(),
            user.getViewportWidth(),
            user.getViewportHeight(),
            user.getDevicePixelRatio(),
            user.getHardwareConcurrency(),
            user.getDeviceMemoryGb(),
            user.getTouchSupport()
        );
        List<UserSuspensionHistory> suspensionHistoryList = suspensionHistoryRepository.findByUserIdOrderByActionAtDesc(userId);
        List<UserBanHistory> banHistoryList = banHistoryRepository.findByUserIdOrderByActionAtDesc(userId);
        long totalSuspensions = suspensionHistoryRepository.countSuspensionsByUserId(userId);
        long totalBans = banHistoryRepository.countBansByUserId(userId);
        List<UserFullDetailsResponse.SuspensionHistoryItem> suspensionHistoryItems = suspensionHistoryList.stream()
                .map(h -> new UserFullDetailsResponse.SuspensionHistoryItem(
                    h.getAction(),
                    h.getReason(),
                    h.getSuspensionType(),
                    h.getSuspendedUntil(),
                    h.getActionAt(),
                    h.getPerformedBy() != null ? h.getPerformedBy().getId() : null,
                    h.getPerformedByEmail()
                ))
                .collect(Collectors.toList());
        List<UserFullDetailsResponse.BanHistoryItem> banHistoryItems = banHistoryList.stream()
                .map(h -> new UserFullDetailsResponse.BanHistoryItem(
                    h.getAction(),
                    h.getReason(),
                    h.getBanType(),
                    h.getBannedUntil(),
                    h.getActionAt(),
                    h.getPerformedBy() != null ? h.getPerformedBy().getId() : null,
                    h.getPerformedByEmail()
                ))
                .collect(Collectors.toList());
        UserFullDetailsResponse.SuspensionBanHistoryInfo suspensionBanHistory = new UserFullDetailsResponse.SuspensionBanHistoryInfo(
            totalSuspensions,
            totalBans,
            suspensionHistoryItems,
            banHistoryItems
        );
        List<UserSubscription> allSubscriptions = userSubscriptionRepository.findByUserIdOrderByStartDateDesc(userId);
        Optional<UserSubscription> activeSubscriptionOpt = userSubscriptionRepository.findActiveSubscriptionByUserId(userId, LocalDateTime.now());
        long totalSubscriptions = allSubscriptions.size();
        long activeSubscriptions = allSubscriptions.stream()
                .filter(s -> "ACTIVE".equals(s.getStatus()) && (s.getEndDate() == null || s.getEndDate().isAfter(LocalDateTime.now())))
                .count();
        long expiredSubscriptions = allSubscriptions.stream()
                .filter(s -> s.isExpired())
                .count();
        long cancelledSubscriptions = allSubscriptions.stream()
                .filter(s -> "CANCELLED".equals(s.getStatus()))
                .count();
        List<UserFullDetailsResponse.SubscriptionDetails> subscriptionDetailsList = allSubscriptions.stream()
                .map(s -> new UserFullDetailsResponse.SubscriptionDetails(
                    s.getId(),
                    s.getPlan().getId(),
                    s.getPlan().getName(),
                    s.getPlan().getCode(),
                    s.getStatus(),
                    s.getStartDate(),
                    s.getEndDate(),
                    s.getCancelledAt(),
                    s.getCancellationReason(),
                    s.getAutoRenew(),
                    s.getLastRenewedAt(),
                    s.getRenewalCount(),
                    s.getPaymentReference(),
                    s.getNotes(),
                    s.isActive(),
                    s.isExpired(),
                    s.getCreatedAt(),
                    s.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        UserFullDetailsResponse.SubscriptionDetails activeSubscriptionDetails = activeSubscriptionOpt.map(s -> 
            new UserFullDetailsResponse.SubscriptionDetails(
                s.getId(),
                s.getPlan().getId(),
                s.getPlan().getName(),
                s.getPlan().getCode(),
                s.getStatus(),
                s.getStartDate(),
                s.getEndDate(),
                s.getCancelledAt(),
                s.getCancellationReason(),
                s.getAutoRenew(),
                s.getLastRenewedAt(),
                s.getRenewalCount(),
                s.getPaymentReference(),
                s.getNotes(),
                s.isActive(),
                s.isExpired(),
                s.getCreatedAt(),
                s.getUpdatedAt()
            )
        ).orElse(null);
        UserFullDetailsResponse.SubscriptionInfo subscriptionInfo = new UserFullDetailsResponse.SubscriptionInfo(
            totalSubscriptions,
            activeSubscriptions,
            expiredSubscriptions,
            cancelledSubscriptions,
            activeSubscriptionDetails,
            subscriptionDetailsList
        );
        return new UserFullDetailsResponse(
            accountInfo,
            profileInfo,
            roleInfo,
            coursesInfo,
            discountsInfo,
            giftsInfo,
            progressInfo,
            statistics,
            preferencesInfo,
            deviceMetadata,
            suspensionBanHistory,
            subscriptionInfo
        );
    }
    @Transactional
    public UserFullDetailsResponse updateUserFullDetails(UUID userId, UserFullDetailsUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (request.email() != null && !request.email().trim().isEmpty()) {
            userRepository.findByEmail(request.email().trim())
                    .ifPresent(u -> {
                        if (!u.getId().equals(userId)) {
                            throw new IllegalStateException("Email already exists.");
                        }
                    });
            user.setEmail(request.email().trim());
        }
        if (request.username() != null && !request.username().trim().isEmpty()) {
            userRepository.findByUsername(request.username().trim())
                    .ifPresent(u -> {
                        if (!u.getId().equals(userId)) {
                            throw new IllegalStateException("Username already exists.");
                        }
                    });
            user.setUsername(request.username().trim());
        }
        if (request.status() != null && !request.status().trim().isEmpty()) {
            user.setStatus(request.status().trim());
        }
        if (request.roleName() != null && !request.roleName().trim().isEmpty()) {
            Role newRole = roleRepository.findByName(request.roleName().trim())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + request.roleName()));
            user.setRole(newRole);
        }
        userRepository.save(user);
        boolean shouldUpdateProfile = request.firstName() != null || request.lastName() != null || 
                                      request.bio() != null || request.avatarUrl() != null || 
                                      request.phoneNumber() != null || request.phoneSocialApp() != null ||
                                      request.secondaryEmail() != null || request.tshirtSize() != null ||
                                      request.extraInfo() != null;
        if (shouldUpdateProfile) {
            Optional<UserProfile> existingProfileOpt = userProfileRepository.findById(userId);
            if (request.secondaryEmail() != null) {
                String secondaryEmail = request.secondaryEmail().trim().isEmpty() ? null : request.secondaryEmail().trim();
                if (secondaryEmail != null) {
                    String currentSecondaryEmail = existingProfileOpt.map(UserProfile::getSecondaryEmail).orElse(null);
                    if (!secondaryEmail.equalsIgnoreCase(currentSecondaryEmail != null ? currentSecondaryEmail : "")) {
                        userProfileRepository.findAll().stream()
                                .filter(p -> p.getSecondaryEmail() != null && 
                                           p.getSecondaryEmail().equalsIgnoreCase(secondaryEmail) &&
                                           !p.getId().equals(userId))
                                .findAny()
                                .ifPresent(p -> {
                                    throw new IllegalStateException("Secondary email already exists.");
                                });
                    }
                }
            }
            if (!existingProfileOpt.isPresent()) {
                UserProfile newProfile = new UserProfile();
                newProfile.setId(userId);
                newProfile.setUser(user);
                newProfile.setFirstName(request.firstName() != null && !request.firstName().trim().isEmpty() ? request.firstName().trim() : null);
                newProfile.setLastName(request.lastName() != null && !request.lastName().trim().isEmpty() ? request.lastName().trim() : null);
                newProfile.setBio(request.bio() != null && !request.bio().trim().isEmpty() ? request.bio().trim() : null);
                String avatarUrl = request.avatarUrl() != null && !request.avatarUrl().trim().isEmpty() ? request.avatarUrl().trim() : null;
                newProfile.setAvatarUrl(avatarUrl != null ? avatarUrl : avatarService.getRandomAvatar(userId));
                newProfile.setPhoneNumber(request.phoneNumber() != null && !request.phoneNumber().trim().isEmpty() ? request.phoneNumber().trim() : null);
                newProfile.setPhoneSocialApp(request.phoneSocialApp() != null && !request.phoneSocialApp().trim().isEmpty() ? request.phoneSocialApp().trim() : null);
                newProfile.setSecondaryEmail(request.secondaryEmail() != null && !request.secondaryEmail().trim().isEmpty() ? request.secondaryEmail().trim() : null);
                newProfile.setTshirtSize(request.tshirtSize() != null && !request.tshirtSize().trim().isEmpty() ? request.tshirtSize().trim() : null);
                String extraInfoValue = null;
                if (request.extraInfo() != null && !request.extraInfo().trim().isEmpty()) {
                    String value = request.extraInfo().trim();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.readTree(value);
                        extraInfoValue = value;  
                    } catch (Exception e) {
                        String escaped = value
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t");
                        extraInfoValue = "\"" + escaped + "\"";
                    }
                }
                newProfile.setExtraInfo(extraInfoValue);
                entityManager.persist(newProfile);
            } else {
                UserProfile existingProfile = existingProfileOpt.get();
                String firstName = request.firstName() != null ? (request.firstName().trim().isEmpty() ? null : request.firstName().trim()) : existingProfile.getFirstName();
                String lastName = request.lastName() != null ? (request.lastName().trim().isEmpty() ? null : request.lastName().trim()) : existingProfile.getLastName();
                String bio = request.bio() != null ? (request.bio().trim().isEmpty() ? null : request.bio().trim()) : existingProfile.getBio();
                String avatarUrl;
                if (request.avatarUrl() != null) {
                    avatarUrl = request.avatarUrl().trim().isEmpty() ? avatarService.getRandomAvatar(userId) : request.avatarUrl().trim();
                } else {
                    avatarUrl = existingProfile.getAvatarUrl() != null ? existingProfile.getAvatarUrl() : avatarService.getRandomAvatar(userId);
                }
                String phoneNumber = request.phoneNumber() != null ? (request.phoneNumber().trim().isEmpty() ? null : request.phoneNumber().trim()) : existingProfile.getPhoneNumber();
                String phoneSocialApp = request.phoneSocialApp() != null ? (request.phoneSocialApp().trim().isEmpty() ? null : request.phoneSocialApp().trim()) : existingProfile.getPhoneSocialApp();
                String secondaryEmail = request.secondaryEmail() != null ? (request.secondaryEmail().trim().isEmpty() ? null : request.secondaryEmail().trim()) : existingProfile.getSecondaryEmail();
                String tshirtSize = request.tshirtSize() != null ? (request.tshirtSize().trim().isEmpty() ? null : request.tshirtSize().trim()) : existingProfile.getTshirtSize();
                String extraInfo = null;
                if (request.extraInfo() != null && !request.extraInfo().trim().isEmpty()) {
                    String extraInfoValue = request.extraInfo().trim();
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.readTree(extraInfoValue);
                        extraInfo = extraInfoValue;  
                    } catch (Exception e) {
                        String escaped = extraInfoValue
                            .replace("\\", "\\\\")
                            .replace("\"", "\\\"")
                            .replace("\n", "\\n")
                            .replace("\r", "\\r")
                            .replace("\t", "\\t");
                        extraInfo = "\"" + escaped + "\"";
                    }
                } else {
                    extraInfo = existingProfile.getExtraInfo();
                }
                userProfileRepository.updateProfileFields(userId, firstName, lastName, bio, avatarUrl, phoneNumber, phoneSocialApp, secondaryEmail, tshirtSize, extraInfo);
            }
        }
        if (request.uiTheme() != null || request.uiLanguage() != null || request.timezone() != null) {
            userPreferenceService.updateUiPreferences(userId,
                    request.uiTheme(), request.uiLanguage(), request.timezone());
        }
        if (request.notifications() != null && !request.notifications().isEmpty()) {
            notificationSettingService.bulkSetForUser(userId, request.notifications());
        }
        return getUserFullDetails(userId);
    }
    @Transactional
    public void submitAppeal(UUID userId, String appealReason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!"SUSPENDED".equals(user.getStatus()) && !"BANNED".equals(user.getStatus())) {
            throw new IllegalStateException("User is not suspended or banned. Cannot submit appeal.");
        }
        if (Boolean.TRUE.equals(user.getAppealRequested()) && "PENDING".equals(user.getAppealStatus())) {
            throw new IllegalStateException("An appeal request is already pending review.");
        }
        user.setAppealRequested(true);
        user.setAppealReason(appealReason);
        user.setAppealRequestedAt(LocalDateTime.now());
        user.setAppealStatus("PENDING");
        user.setAppealReviewedAt(null);
        user.setAppealReviewedBy(null);
        userRepository.save(user);
    }
    @Transactional
    public void reviewAppeal(UUID userId, String decision, UUID reviewerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!Boolean.TRUE.equals(user.getAppealRequested()) || !"PENDING".equals(user.getAppealStatus())) {
            throw new IllegalStateException("No pending appeal request found for this user.");
        }
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("Reviewer not found."));
        String decisionUpper = decision != null ? decision.trim().toUpperCase() : "";
        if (!"APPROVED".equals(decisionUpper) && !"REJECTED".equals(decisionUpper)) {
            throw new IllegalArgumentException("Decision must be either 'APPROVED' or 'REJECTED'.");
        }
        user.setAppealStatus(decisionUpper);
        user.setAppealReviewedAt(LocalDateTime.now());
        user.setAppealReviewedBy(reviewer);
        if ("APPROVED".equals(decisionUpper)) {
            user.setStatus("ACTIVE");
            user.setSuspendedAt(null);
            user.setSuspendReason(null);
            user.setSuspendedUntil(null);
            user.setSuspensionType(null);
            user.setBannedAt(null);
            user.setBanReason(null);
            user.setBannedUntil(null);
            user.setBanType(null);
        }
        userRepository.save(user);
    }
    @Transactional(readOnly = true)
    public List<User> getPendingAppeals() {
        return userRepository.findByAppealRequestedTrueAndAppealStatus("PENDING");
    }
    @Transactional
    public Map<String, Object> checkAndReactivateExpiredSuspensionsAndBans() {
        LocalDateTime now = LocalDateTime.now();
        System.out.println("[UserAdminService] ⏰ Checking expired suspensions/bans at: " + now);
        int reactivatedCount = 0;
        List<Map<String, Object>> reactivatedUsers = new ArrayList<>();
        List<User> expiredSuspensions = userRepository.findUsersWithExpiredSuspensions(now);
        System.out.println("[UserAdminService] Found " + expiredSuspensions.size() + " expired suspensions");
        List<User> allSuspended = userRepository.findByStatus("SUSPENDED");
        System.out.println("[UserAdminService] Total SUSPENDED users: " + allSuspended.size());
        for (User u : allSuspended) {
            System.out.println("[UserAdminService]   - " + u.getEmail() + 
                " | Type: " + u.getSuspensionType() + 
                " | Until: " + u.getSuspendedUntil() + 
                " | Is expired: " + (u.getSuspendedUntil() != null && u.getSuspendedUntil().isBefore(now)));
        }
        for (User user : expiredSuspensions) {
            System.out.println("[UserAdminService] Reactivating suspended user: " + user.getEmail() + " (suspendedUntil: " + user.getSuspendedUntil() + ")");
            UserSuspensionHistory history = new UserSuspensionHistory();
            history.setUser(user);
            history.setAction("REACTIVATE");
            history.setReason("Automatic reactivation: Suspension period expired");
            history.setSuspensionType(user.getSuspensionType());
            history.setSuspendedUntil(user.getSuspendedUntil());
            history.setActionAt(now);
            history.setPerformedBy(null);  
            history.setPerformedByEmail("System (Automatic)");
            suspensionHistoryRepository.save(history);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("username", user.getUsername());
            userInfo.put("action", "SUSPENDED");
            userInfo.put("reason", user.getSuspendReason());
            userInfo.put("suspendedAt", user.getSuspendedAt());
            userInfo.put("suspendedUntil", user.getSuspendedUntil());
            userInfo.put("suspensionType", user.getSuspensionType());
            user.setStatus("ACTIVE");
            user.setSuspendedUntil(null);
            userRepository.save(user);
            reactivatedUsers.add(userInfo);
            reactivatedCount++;
        }
        List<User> expiredBans = userRepository.findUsersWithExpiredBans(now);
        System.out.println("[UserAdminService] Found " + expiredBans.size() + " expired bans");
        List<User> allBanned = userRepository.findByStatus("BANNED");
        System.out.println("[UserAdminService] Total BANNED users: " + allBanned.size());
        for (User u : allBanned) {
            System.out.println("[UserAdminService]   - " + u.getEmail() + 
                " | Type: " + u.getBanType() + 
                " | Until: " + u.getBannedUntil() + 
                " | Is expired: " + (u.getBannedUntil() != null && u.getBannedUntil().isBefore(now)));
        }
        for (User user : expiredBans) {
            System.out.println("[UserAdminService] Reactivating banned user: " + user.getEmail() + " (bannedUntil: " + user.getBannedUntil() + ")");
            UserBanHistory history = new UserBanHistory();
            history.setUser(user);
            history.setAction("REACTIVATE");
            history.setReason("Automatic reactivation: Ban period expired");
            history.setBanType(user.getBanType());
            history.setBannedUntil(user.getBannedUntil());
            history.setActionAt(now);
            history.setPerformedBy(null);  
            history.setPerformedByEmail("System (Automatic)");
            banHistoryRepository.save(history);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getId());
            userInfo.put("email", user.getEmail());
            userInfo.put("username", user.getUsername());
            userInfo.put("action", "BANNED");
            userInfo.put("reason", user.getBanReason());
            userInfo.put("bannedAt", user.getBannedAt());
            userInfo.put("bannedUntil", user.getBannedUntil());
            userInfo.put("banType", user.getBanType());
            user.setStatus("ACTIVE");
            user.setBannedUntil(null);
            userRepository.save(user);
            reactivatedUsers.add(userInfo);
            reactivatedCount++;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("reactivatedCount", reactivatedCount);
        result.put("reactivatedUsers", reactivatedUsers);
        System.out.println("[UserAdminService] ✅ Check completed. Reactivated: " + reactivatedCount + " user(s)");
        return result;
    }
    @Transactional
    public boolean checkAndReactivateUserIfExpired(User user) {
        if (user == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        boolean wasReactivated = false;
        if ("SUSPENDED".equals(user.getStatus()) && 
            "TEMPORARY".equals(user.getSuspensionType()) && 
            user.getSuspendedUntil() != null && 
            user.getSuspendedUntil().isBefore(now)) {
            UserSuspensionHistory history = new UserSuspensionHistory();
            history.setUser(user);
            history.setAction("REACTIVATE");
            history.setReason("Automatic reactivation: Suspension period expired");
            history.setSuspensionType(user.getSuspensionType());
            history.setSuspendedUntil(user.getSuspendedUntil());
            history.setActionAt(now);
            history.setPerformedBy(null);
            history.setPerformedByEmail("System (Automatic)");
            suspensionHistoryRepository.save(history);
            user.setStatus("ACTIVE");
            user.setSuspendedUntil(null);
            userRepository.save(user);
            wasReactivated = true;
        }
        if ("BANNED".equals(user.getStatus()) && 
            "TEMPORARY".equals(user.getBanType()) && 
            user.getBannedUntil() != null && 
            user.getBannedUntil().isBefore(now)) {
            UserBanHistory history = new UserBanHistory();
            history.setUser(user);
            history.setAction("REACTIVATE");
            history.setReason("Automatic reactivation: Ban period expired");
            history.setBanType(user.getBanType());
            history.setBannedUntil(user.getBannedUntil());
            history.setActionAt(now);
            history.setPerformedBy(null);
            history.setPerformedByEmail("System (Automatic)");
            banHistoryRepository.save(history);
            user.setStatus("ACTIVE");
            user.setBannedUntil(null);
            userRepository.save(user);
            wasReactivated = true;
        }
        return wasReactivated;
    }
}