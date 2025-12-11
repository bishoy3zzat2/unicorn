package com.loyalixa.backend.financial.dto;
import com.loyalixa.backend.user.dto.RoleResponse;
import com.loyalixa.backend.user.dto.PermissionResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
public record ProfileResponse(
    UUID id,
    String username,
    String email,
    String status,
    LocalDateTime createdAt,
    LocalDateTime lastLoginAt,
    RoleResponse role,
    Set<PermissionResponse> permissions,
    String firstName,
    String lastName,
    String bio,
    String avatarUrl,
    String phoneNumber,
    List<CourseInfo> coursesAsInstructor,
    List<CourseInfo> coursesAsMentor,
    List<DiscountCodeInfo> discountCodesCreated,
    FinancialAccountResponse financialAccount,
    ProfileStatistics statistics
) {
    public record CourseInfo(
        UUID id,
        String title,
        String slug,
        String status
    ) {}
    public record DiscountCodeInfo(
        UUID id,
        String code,
        String discountType,
        String status,
        Integer currentUses,
        Integer maxUses
    ) {}
    public record ProfileStatistics(
        int totalCoursesAsInstructor,
        int totalCoursesAsMentor,
        int totalDiscountCodesCreated,
        int totalEnrollments
    ) {}
}
