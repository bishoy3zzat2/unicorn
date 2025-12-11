package com.loyalixa.backend.financial;

import com.loyalixa.backend.course.CourseRepository;
import com.loyalixa.backend.course.EnrollmentRepository;
import com.loyalixa.backend.discount.DiscountCodeRepository;
import com.loyalixa.backend.financial.dto.*;
import com.loyalixa.backend.security.Role;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserProfile;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.user.dto.PermissionResponse;
import com.loyalixa.backend.user.dto.RoleResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfileService {


        private final UserRepository userRepository;
        private final CourseRepository courseRepository;
        private final EnrollmentRepository enrollmentRepository;
        private final DiscountCodeRepository discountCodeRepository;
        private final FinancialAccountRepository financialAccountRepository;

        public ProfileService(
                        UserRepository userRepository,
                        CourseRepository courseRepository,
                        EnrollmentRepository enrollmentRepository,
                        DiscountCodeRepository discountCodeRepository,
                        FinancialAccountRepository financialAccountRepository) {
                this.userRepository = userRepository;
                this.courseRepository = courseRepository;
                this.enrollmentRepository = enrollmentRepository;
                this.discountCodeRepository = discountCodeRepository;
                this.financialAccountRepository = financialAccountRepository;
        }

        @Transactional(readOnly = true)
        public ProfileResponse getMyProfile(User currentUser) {
                // Load user with profile and role
                User user = userRepository.findById(currentUser.getId())
                                .orElseThrow(() -> new IllegalStateException("User not found"));

                // Get user profile
                UserProfile profile = user.getUserProfile();

                // Get role and permissions
                Role role = user.getRole();
                RoleResponse roleResponse = null;
                Set<PermissionResponse> permissions = null;

                if (role != null) {
                        roleResponse = new RoleResponse(
                                        role.getId(),
                                        role.getName(),
                                        role.getDescription());

                        if (role.getPermissions() != null) {
                                permissions = role.getPermissions().stream()
                                                .map(p -> new PermissionResponse(
                                                                p.getId(),
                                                                p.getName(),
                                                                p.getDescription()))
                                                .collect(Collectors.toSet());
                        }
                }

                // Get courses as instructor
                List<ProfileResponse.CourseInfo> coursesAsInstructor = courseRepository.findAll().stream()
                                .filter(course -> course.getInstructors() != null &&
                                                course.getInstructors().stream()
                                                                .anyMatch(instructor -> instructor.getId()
                                                                                .equals(user.getId())))
                                .map(course -> new ProfileResponse.CourseInfo(
                                                course.getId(),
                                                course.getTitle(),
                                                course.getSlug(),
                                                course.getStatus()))
                                .collect(Collectors.toList());

                // Get courses as mentor (moderator)
                List<ProfileResponse.CourseInfo> coursesAsMentor = courseRepository.findAll().stream()
                                .filter(course -> course.getModerators() != null &&
                                                course.getModerators().stream()
                                                                .anyMatch(moderator -> moderator.getId()
                                                                                .equals(user.getId())))
                                .map(course -> new ProfileResponse.CourseInfo(
                                                course.getId(),
                                                course.getTitle(),
                                                course.getSlug(),
                                                course.getStatus()))
                                .collect(Collectors.toList());

                // Get discount codes created by user
                List<ProfileResponse.DiscountCodeInfo> discountCodesCreated = discountCodeRepository
                                .findByCreatedByUserId(user.getId()).stream()
                                .map(code -> new ProfileResponse.DiscountCodeInfo(
                                                code.getId(),
                                                code.getCode(),
                                                code.getDiscountType(),
                                                "ACTIVE", // You might want to add a status field to DiscountCode
                                                code.getCurrentUses(),
                                                code.getMaxUses()))
                                .collect(Collectors.toList());

                // Get financial account
                FinancialAccountResponse financialAccount = null;
                Optional<FinancialAccount> accountOpt = financialAccountRepository.findByUserId(user.getId());
                if (accountOpt.isPresent()) {
                        FinancialAccount account = accountOpt.get();
                        financialAccount = new FinancialAccountResponse(
                                        account.getId(),
                                        account.getUser().getId(),
                                        account.getBalance(),
                                        account.getPaymentMethod(),
                                        account.getEmploymentType(),
                                        account.getHasFixedSalary(),
                                        account.getEmploymentStartDate(),
                                        account.getEmploymentEndDate(),
                                        account.getWorkSchedule(),
                                        account.getHoursPerWeek(),
                                        account.getWorkInstructions(),
                                        account.getMonthlySalary(),
                                        account.getSalaryCurrency(),
                                        account.getSalaryPaymentDay(),
                                        account.getMonthlyBonus(),
                                        account.getCurrency(),
                                        account.getBankName(),
                                        account.getBankAccountNumber(),
                                        account.getBankIban(),
                                        account.getBankSwiftCode(),
                                        account.getWalletType(),
                                        account.getWalletNumber(),
                                        account.getCardType(),
                                        account.getCardNumber(),
                                        account.getCardHolderName(),
                                        account.getCardCountry(),
                                        account.getCardBankName(),
                                        account.getCardExpiryDate(),
                                        account.getStatus(),
                                        account.getNotes());
                }

                // Get enrollments count
                long totalEnrollments = enrollmentRepository.findByStudentId(user.getId()).size();

                // Build statistics
                ProfileResponse.ProfileStatistics statistics = new ProfileResponse.ProfileStatistics(
                                coursesAsInstructor.size(),
                                coursesAsMentor.size(),
                                discountCodesCreated.size(),
                                (int) totalEnrollments);

                return new ProfileResponse(
                                user.getId(),
                                user.getActualUsername(), // Use getActualUsername() instead of getUsername() to get the
                                                          // real username, not email
                                user.getEmail(),
                                user.getStatus(),
                                user.getCreatedAt(),
                                user.getLastLoginAt(),
                                roleResponse,
                                permissions,
                                profile != null ? profile.getFirstName() : null,
                                profile != null ? profile.getLastName() : null,
                                profile != null ? profile.getBio() : null,
                                profile != null ? profile.getAvatarUrl() : null,
                                profile != null ? profile.getPhoneNumber() : null,
                                coursesAsInstructor,
                                coursesAsMentor,
                                discountCodesCreated,
                                financialAccount,
                                statistics);
        }
}
