package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.BadgeAdminResponse;
import com.loyalixa.backend.course.dto.BadgeDetailsResponse;
import com.loyalixa.backend.course.dto.BadgeRequest;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.stream.Collectors;
@Service
public class BadgeService {
    private final BadgeRepository badgeRepository;
    public BadgeService(BadgeRepository badgeRepository) {
        this.badgeRepository = badgeRepository;
    }
    @Transactional
    public Badge createBadge(BadgeRequest request, User adminUser) {
        String customCss = null;
        if (request.customCss() != null && !request.customCss().trim().isEmpty()) {
            customCss = request.customCss().trim();
        } else if (request.color() != null && !request.color().trim().isEmpty()) {
            customCss = "background: " + request.color() + "; color: #ffffff; border-radius: 12px; padding: 8px 16px; font-weight: 600; font-size: 14px;";
        }
        Double weight = request.weight() != null ? request.weight() : 0.0;
        Duration usageDuration = null;
        if (request.usageDurationMinutes() != null && request.usageDurationMinutes() > 0) {
            usageDuration = Duration.ofMinutes(request.usageDurationMinutes());
        }
        String targetType = request.targetType();
        if (targetType == null || targetType.trim().isEmpty()) {
            targetType = "COURSE";
        } else {
            targetType = targetType.toUpperCase();
        }
        Long usageDurationMinutes = usageDuration != null ? usageDuration.toMinutes() : null;
        List<Badge> existingBadges = badgeRepository.findAll();
        for (Badge existing : existingBadges) {
            if (existing.getName() != null && existing.getName().equalsIgnoreCase(request.name())) {
                String existingCustomCss = existing.getCustomCss() != null ? existing.getCustomCss().trim() : "";
                String newCustomCss = customCss != null ? customCss.trim() : "";
                if (!existingCustomCss.equals(newCustomCss)) {
                    continue;  
                }
                Double existingWeight = existing.getWeight() != null ? existing.getWeight() : 0.0;
                if (!existingWeight.equals(weight)) {
                    continue;  
                }
                if ((existing.getExpirationDate() == null) != (request.expirationDate() == null)) {
                    continue;  
                }
                if (existing.getExpirationDate() != null && request.expirationDate() != null) {
                    if (!existing.getExpirationDate().equals(request.expirationDate())) {
                        continue;  
                    }
                }
                Long existingDurationMinutes = existing.getUsageDuration() != null ? existing.getUsageDuration().toMinutes() : null;
                if ((existingDurationMinutes == null) != (usageDurationMinutes == null)) {
                    continue;  
                }
                if (existingDurationMinutes != null && usageDurationMinutes != null) {
                    if (!existingDurationMinutes.equals(usageDurationMinutes)) {
                        continue;  
                    }
                }
                String existingTargetType = existing.getTargetType() != null ? existing.getTargetType().toUpperCase() : "COURSE";
                if (!existingTargetType.equals(targetType)) {
                    continue;  
                }
                throw new IllegalStateException("A badge with the same name and properties already exists.");
            }
        }
        Badge newBadge = new Badge();
        newBadge.setName(request.name());
        if (customCss != null) {
            newBadge.setCustomCss(customCss);
            if (request.color() != null && !request.color().trim().isEmpty()) {
                newBadge.setColorCode(request.color());
            }
        }
        newBadge.setWeight(weight);
        newBadge.setExpirationDate(request.expirationDate());
        newBadge.setCreatedBy(adminUser);
        newBadge.setUsageDuration(usageDuration);
        newBadge.setTargetType(targetType);
        newBadge.setIsDynamic(false);
        return badgeRepository.save(newBadge);
    }
    @Transactional
    public Badge createBadge(BadgeRequest request) {
        return createBadge(request, null);
    }
    @Transactional
    public Badge updateBadge(UUID badgeId, BadgeRequest request, User adminUser) {
        Badge badge = badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found."));
        String customCss = null;
        if (request.customCss() != null && !request.customCss().trim().isEmpty()) {
            customCss = request.customCss().trim();
        } else if (request.color() != null && !request.color().trim().isEmpty()) {
            customCss = "background: " + request.color() + "; color: #ffffff; border-radius: 12px; padding: 8px 16px; font-weight: 600; font-size: 14px;";
        } else {
            customCss = badge.getCustomCss();
        }
        Double weight = request.weight() != null ? request.weight() : badge.getWeight();
        Duration usageDuration = null;
        if (request.usageDurationMinutes() != null && request.usageDurationMinutes() > 0) {
            usageDuration = Duration.ofMinutes(request.usageDurationMinutes());
        } else if (request.usageDurationMinutes() != null && request.usageDurationMinutes() == 0) {
            usageDuration = null;  
        } else {
            usageDuration = badge.getUsageDuration();
        }
        String targetType = request.targetType();
        if (targetType != null && !targetType.trim().isEmpty()) {
            targetType = targetType.toUpperCase();
        } else if (badge.getTargetType() != null) {
            targetType = badge.getTargetType();  
        } else {
            targetType = "COURSE";  
        }
        LocalDateTime expirationDate = request.expirationDate() != null ? request.expirationDate() : badge.getExpirationDate();
        Long usageDurationMinutes = usageDuration != null ? usageDuration.toMinutes() : null;
        List<Badge> existingBadges = badgeRepository.findAll();
        for (Badge existing : existingBadges) {
            if (existing.getId().equals(badgeId)) {
                continue;
            }
            if (existing.getName() != null && existing.getName().equalsIgnoreCase(request.name())) {
                String existingCustomCss = existing.getCustomCss() != null ? existing.getCustomCss().trim() : "";
                String newCustomCss = customCss != null ? customCss.trim() : "";
                if (!existingCustomCss.equals(newCustomCss)) {
                    continue;  
                }
                Double existingWeight = existing.getWeight() != null ? existing.getWeight() : 0.0;
                if (!existingWeight.equals(weight)) {
                    continue;  
                }
                if ((existing.getExpirationDate() == null) != (expirationDate == null)) {
                    continue;  
                }
                if (existing.getExpirationDate() != null && expirationDate != null) {
                    if (!existing.getExpirationDate().equals(expirationDate)) {
                        continue;  
                    }
                }
                Long existingDurationMinutes = existing.getUsageDuration() != null ? existing.getUsageDuration().toMinutes() : null;
                if ((existingDurationMinutes == null) != (usageDurationMinutes == null)) {
                    continue;  
                }
                if (existingDurationMinutes != null && usageDurationMinutes != null) {
                    if (!existingDurationMinutes.equals(usageDurationMinutes)) {
                        continue;  
                    }
                }
                String existingTargetType = existing.getTargetType() != null ? existing.getTargetType().toUpperCase() : "COURSE";
                if (!existingTargetType.equals(targetType)) {
                    continue;  
                }
                throw new IllegalStateException("Another badge with the same name and properties already exists.");
            }
        }
        badge.setName(request.name());
        if (request.customCss() != null && !request.customCss().trim().isEmpty()) {
            badge.setCustomCss(request.customCss().trim());
        } else if (request.color() != null && !request.color().trim().isEmpty()) {
            badge.setColorCode(request.color());
            if (badge.getCustomCss() == null || badge.getCustomCss().trim().isEmpty()) {
                badge.setCustomCss("background: " + request.color() + "; color: #ffffff; border-radius: 12px; padding: 8px 16px; font-weight: 600; font-size: 14px;");
            }
        }
        badge.setWeight(weight);
        badge.setExpirationDate(expirationDate);
        badge.setUpdatedBy(adminUser);
        badge.setUsageDuration(usageDuration);
        badge.setTargetType(targetType);
        Badge savedBadge = badgeRepository.save(badge);
        return savedBadge;
    }
    @Transactional
    public Badge updateBadge(UUID badgeId, BadgeRequest request) {
        return updateBadge(badgeId, request, null);
    }
    public BadgeAdminResponse toAdminResponse(Badge badge) {
        return mapToAdminResponse(badge);
    }
    @Transactional
    public void deleteBadge(UUID badgeId) {
        Badge badge = badgeRepository.findByIdWithRelations(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found."));
        if (badge.getCoursesUsingBadge() != null && !badge.getCoursesUsingBadge().isEmpty()) {
            int courseCount = badge.getCoursesUsingBadge().size();
            throw new IllegalStateException(
                "Cannot delete badge. It is currently associated with " + courseCount + 
                " course(s). Please remove the badge from all courses before deleting."
            );
        }
        badgeRepository.delete(badge);
    }
    @Transactional(readOnly = true)
    public List<BadgeAdminResponse> getAllBadges() { 
        return getAllBadgesResponse();
    }
    @Transactional(readOnly = true)
    public BadgeDetailsResponse getBadgeDetails(UUID badgeId) {
        Badge badge = badgeRepository.findByIdWithRelations(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found."));
        List<BadgeDetailsResponse.CourseInfo> courseInfos = new ArrayList<>();
        if (badge.getCoursesUsingBadge() != null && !badge.getCoursesUsingBadge().isEmpty()) {
            courseInfos = badge.getCoursesUsingBadge().stream()
                    .map(cb -> new BadgeDetailsResponse.CourseInfo(
                        cb.getCourse().getId(),
                        cb.getCourse().getTitle(),
                        cb.getCourse().getSlug(),
                        cb.getCourse().getStatus(),
                        cb.getAssignedAt(),
                        cb.getExpirationDate()
                    ))
                    .collect(Collectors.toList());
        }
        BadgeDetailsResponse.UserInfo createdByInfo = null;
        if (badge.getCreatedBy() != null) {
            User createdBy = badge.getCreatedBy();
            createdByInfo = new BadgeDetailsResponse.UserInfo(
                createdBy.getId(),
                createdBy.getEmail(),
                createdBy.getUsername(),
                createdBy.getRole() != null ? createdBy.getRole().getName() : null
            );
        }
        BadgeDetailsResponse.UserInfo updatedByInfo = null;
        if (badge.getUpdatedBy() != null) {
            User updatedBy = badge.getUpdatedBy();
            updatedByInfo = new BadgeDetailsResponse.UserInfo(
                updatedBy.getId(),
                updatedBy.getEmail(),
                updatedBy.getUsername(),
                updatedBy.getRole() != null ? updatedBy.getRole().getName() : null
            );
        }
        return new BadgeDetailsResponse(
            badge.getId(),
            badge.getName(),
            badge.getColorCode(),
            badge.getCustomCss(),
            badge.getWeight(),
            badge.getIconClass(),
            badge.getTargetType(),
            badge.getIsDynamic(),
            badge.getExpirationDate(),
            badge.getValidUntil(),
            badge.getUsageDuration() != null ? badge.getUsageDuration().toMinutes() : null,
            courseInfos,
            createdByInfo,
            updatedByInfo,
            badge.getCreatedAt(),
            badge.getUpdatedAt()
        );
    }
    @Transactional(readOnly = true)
    public Badge getBadgeById(UUID badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new IllegalArgumentException("Badge not found."));
    }
    private BadgeAdminResponse mapToAdminResponse(Badge badge) {
        return new BadgeAdminResponse(
            badge.getId(),
            badge.getName(),
            badge.getColorCode(),
            badge.getCustomCss(),
            badge.getWeight(),
            badge.getTargetType(),
            badge.getExpirationDate() != null ? badge.getExpirationDate() : badge.getValidUntil(),  
            badge.getUsageDuration() != null ? badge.getUsageDuration().toMinutes() : null  
        );
    }
    @Transactional(readOnly = true)
    public List<BadgeAdminResponse> getAllBadgesResponse() {
        List<Badge> badges = badgeRepository.findAllWithCreatedAndUpdatedBy();
        return badges.stream()
                .map(this::mapToAdminResponse)
                .collect(Collectors.toList());
    }
}