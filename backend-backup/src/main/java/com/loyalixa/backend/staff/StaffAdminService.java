package com.loyalixa.backend.staff;
import com.loyalixa.backend.financial.*;
import com.loyalixa.backend.financial.dto.*;
import com.loyalixa.backend.security.Role;
import com.loyalixa.backend.security.RoleRepository;
import com.loyalixa.backend.staff.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.user.UserService;
import com.loyalixa.backend.user.UserAdminService;
import com.loyalixa.backend.user.dto.UserAdminResponse;
import com.loyalixa.backend.user.preferences.UserPreferenceService;
import com.loyalixa.backend.user.preferences.NotificationSettingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class StaffAdminService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final UserAdminService userAdminService;
    private final FinancialService financialService;
    private final FinancialAccountRepository financialAccountRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TaskPaymentRepository taskPaymentRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferenceService userPreferenceService;
    private final NotificationSettingService notificationSettingService;
    private final StaffRepository staffRepository;  
    public StaffAdminService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserService userService,
            UserAdminService userAdminService,
            FinancialService financialService,
            FinancialAccountRepository financialAccountRepository,
            PaymentRequestRepository paymentRequestRepository,
            TaskPaymentRepository taskPaymentRepository,
            FinancialTransactionRepository financialTransactionRepository,
            PasswordEncoder passwordEncoder,
            UserPreferenceService userPreferenceService,
            NotificationSettingService notificationSettingService,
            StaffRepository staffRepository) {  
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.userAdminService = userAdminService;
        this.financialService = financialService;
        this.financialAccountRepository = financialAccountRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.taskPaymentRepository = taskPaymentRepository;
        this.financialTransactionRepository = financialTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPreferenceService = userPreferenceService;
        this.notificationSettingService = notificationSettingService;
        this.staffRepository = staffRepository;  
    }
    @Transactional(readOnly = true)
    public List<UserAdminResponse> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() != null && !"STUDENT".equalsIgnoreCase(user.getRole().getName()))
                .map(userAdminService::mapToUserAdminResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<UserAdminResponse> searchStaff(StaffSearchRequest request) {
        List<User> allUsers = userRepository.findAll();
        return allUsers.stream()
                .filter(user -> {
                    if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
                        return false;
                    }
                    if (request.role() != null && !request.role().isEmpty()) {
                        String userRole = user.getRole() != null ? user.getRole().getName() : null;
                        if (!request.role().equalsIgnoreCase(userRole)) {
                            return false;
                        }
                    }
                    if (request.roleId() != null) {
                        UUID userRoleId = user.getRole() != null ? user.getRole().getId() : null;
                        if (!request.roleId().equals(userRoleId)) {
                            return false;
                        }
                    }
                    if (request.status() != null && !request.status().isEmpty()) {
                        if (!request.status().equalsIgnoreCase(user.getStatus())) {
                            return false;
                        }
                    }
                    if (request.search() != null && !request.search().isEmpty()) {
                        String search = request.search().toLowerCase();
                        String email = (user.getEmail() != null ? user.getEmail() : "").toLowerCase();
                        String username = (user.getUsername() != null ? user.getUsername() : "").toLowerCase();
                        if (!email.contains(search) && !username.contains(search)) {
                            return false;
                        }
                    }
                    if (request.taskStatus() != null || request.hasPendingTasks() != null || 
                        request.hasRejectedTasks() != null || request.hasCompletedTasks() != null) {
                        List<TaskPayment> userTasks = taskPaymentRepository.findByUserId(user.getId());
                        if (request.taskStatus() != null && !request.taskStatus().isEmpty()) {
                            boolean hasTaskWithStatus = userTasks.stream()
                                    .anyMatch(task -> request.taskStatus().equalsIgnoreCase(task.getStatus()));
                            if (!hasTaskWithStatus) {
                                return false;
                            }
                        }
                        if (request.hasPendingTasks() != null && request.hasPendingTasks()) {
                            boolean hasPending = userTasks.stream()
                                    .anyMatch(task -> "PENDING".equalsIgnoreCase(task.getStatus()));
                            if (!hasPending) {
                                return false;
                            }
                        }
                        if (request.hasRejectedTasks() != null && request.hasRejectedTasks()) {
                            boolean hasRejected = userTasks.stream()
                                    .anyMatch(task -> "REJECTED".equalsIgnoreCase(task.getStatus()));
                            if (!hasRejected) {
                                return false;
                            }
                        }
                        if (request.hasCompletedTasks() != null && request.hasCompletedTasks()) {
                            boolean hasCompleted = userTasks.stream()
                                    .anyMatch(task -> "APPROVED".equalsIgnoreCase(task.getStatus()) || 
                                                    "PAID".equalsIgnoreCase(task.getStatus()));
                            if (!hasCompleted) {
                                return false;
                            }
                        }
                    }
                    if (request.paymentRequestStatus() != null || request.hasPaymentRequests() != null) {
                        List<PaymentRequest> userPaymentRequests = paymentRequestRepository.findByUserId(user.getId());
                        if (request.paymentRequestStatus() != null && !request.paymentRequestStatus().isEmpty()) {
                            boolean hasRequestWithStatus = userPaymentRequests.stream()
                                    .anyMatch(req -> request.paymentRequestStatus().equalsIgnoreCase(req.getStatus()));
                            if (!hasRequestWithStatus) {
                                return false;
                            }
                        }
                        if (request.hasPaymentRequests() != null) {
                            boolean hasRequests = !userPaymentRequests.isEmpty();
                            if (request.hasPaymentRequests() && !hasRequests) {
                                return false;
                            }
                            if (!request.hasPaymentRequests() && hasRequests) {
                                return false;
                            }
                        }
                    }
                    if (request.hasDiscounts() != null || request.hasBonuses() != null || 
                        request.hasPenalties() != null || request.hasDeposits() != null) {
                        List<com.loyalixa.backend.financial.FinancialTransaction> userTransactions = 
                                financialTransactionRepository.findByUserId(user.getId());
                        if (request.hasDiscounts() != null && request.hasDiscounts()) {
                            boolean hasDiscount = userTransactions.stream()
                                    .anyMatch(tx -> tx.getTransactionType() != null && 
                                                  tx.getTransactionType().toUpperCase().contains("DISCOUNT"));
                            if (!hasDiscount) {
                                return false;
                            }
                        }
                        if (request.hasBonuses() != null && request.hasBonuses()) {
                            boolean hasBonus = userTransactions.stream()
                                    .anyMatch(tx -> tx.getTransactionType() != null && 
                                                  tx.getTransactionType().toUpperCase().contains("BONUS"));
                            if (!hasBonus) {
                                return false;
                            }
                        }
                        if (request.hasPenalties() != null && request.hasPenalties()) {
                            boolean hasPenalty = userTransactions.stream()
                                    .anyMatch(tx -> tx.getTransactionType() != null && 
                                                  tx.getTransactionType().toUpperCase().contains("PENALTY"));
                            if (!hasPenalty) {
                                return false;
                            }
                        }
                        if (request.hasDeposits() != null && request.hasDeposits()) {
                            boolean hasDeposit = userTransactions.stream()
                                    .anyMatch(tx -> tx.getTransactionType() != null && 
                                                  tx.getTransactionType().toUpperCase().contains("DEPOSIT"));
                            if (!hasDeposit) {
                                return false;
                            }
                        }
                    }
                    return true;
                })
                .map(userAdminService::mapToUserAdminResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<UserAdminResponse> getStaffPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        return users.stream()
                .filter(user -> user.getRole() != null && !"STUDENT".equalsIgnoreCase(user.getRole().getName()))
                .map(userAdminService::mapToUserAdminResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public StaffResponse getStaffDetails(UUID userId) {
        User user = userRepository.findByIdWithStaff(userId)
                .orElseGet(() -> userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalStateException("User not found")));
        if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalStateException("User is not a staff member");
        }
        FinancialAccount account = financialAccountRepository.findByUserId(userId)
                .orElse(null);
        FinancialAccountResponse accountResponse = account != null 
                ? mapToAccountResponse(account) 
                : null;
        List<PaymentRequestResponse> pendingRequests = paymentRequestRepository
                .findByUserIdAndStatus(userId, "PENDING").stream()
                .map(this::mapToPaymentRequestResponse)
                .collect(Collectors.toList());
        List<TaskPayment> allTasks = taskPaymentRepository.findByUserId(userId);
        List<TaskPaymentResponse> allTasksResponse = allTasks.stream()
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
        List<TaskPaymentResponse> pendingTasks = allTasks.stream()
                .filter(t -> "PENDING".equalsIgnoreCase(t.getStatus()))
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
        List<TaskPaymentResponse> completedTasks = allTasks.stream()
                .filter(t -> "APPROVED".equalsIgnoreCase(t.getStatus()) || "PAID".equalsIgnoreCase(t.getStatus()))
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
        List<TaskPaymentResponse> rejectedTasks = allTasks.stream()
                .filter(t -> "REJECTED".equalsIgnoreCase(t.getStatus()))
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
        long totalTasks = allTasks.size();
        long completedTasksCount = completedTasks.size();
        long pendingTasksCount = pendingTasks.size();
        long rejectedTasksCount = rejectedTasks.size();
        List<com.loyalixa.backend.financial.FinancialTransaction> allTransactions = 
                account != null ? financialTransactionRepository.findByUserId(userId) : List.of();
        long totalTransactions = allTransactions.size();
        java.math.BigDecimal totalDeposits = allTransactions.stream()
                .filter(tx -> tx.getTransactionType() != null && 
                             tx.getTransactionType().toUpperCase().contains("DEPOSIT"))
                .map(com.loyalixa.backend.financial.FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalWithdrawals = allTransactions.stream()
                .filter(tx -> tx.getTransactionType() != null && 
                             (tx.getTransactionType().toUpperCase().contains("WITHDRAWAL") ||
                              tx.getTransactionType().toUpperCase().contains("ADVANCE")))
                .map(com.loyalixa.backend.financial.FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalDiscounts = allTransactions.stream()
                .filter(tx -> tx.getTransactionType() != null && 
                             tx.getTransactionType().toUpperCase().contains("DISCOUNT"))
                .map(com.loyalixa.backend.financial.FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalBonuses = allTransactions.stream()
                .filter(tx -> tx.getTransactionType() != null && 
                             tx.getTransactionType().toUpperCase().contains("BONUS"))
                .map(com.loyalixa.backend.financial.FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        java.math.BigDecimal totalPenalties = allTransactions.stream()
                .filter(tx -> tx.getTransactionType() != null && 
                             tx.getTransactionType().toUpperCase().contains("PENALTY"))
                .map(com.loyalixa.backend.financial.FinancialTransaction::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        List<com.loyalixa.backend.financial.dto.FinancialTransactionResponse> recentTransactions = 
                allTransactions.stream()
                        .limit(10)
                        .map(this::mapToTransactionResponse)
                        .collect(Collectors.toList());
        return new StaffResponse(
                userAdminService.mapToUserAdminResponse(user),
                accountResponse,
                pendingRequests,
                allTasksResponse,
                pendingTasks,
                completedTasks,
                rejectedTasks,
                totalTasks,
                completedTasksCount,
                pendingTasksCount,
                rejectedTasksCount,
                totalTransactions,
                totalDeposits,
                totalWithdrawals,
                totalDiscounts,
                totalBonuses,
                totalPenalties,
                recentTransactions
        );
    }
    @Transactional
    public UserAdminResponse createStaff(StaffCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Email already in use");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalStateException("Username already taken");
        }
        Role role = roleRepository.findById(request.roleId())
                .orElseThrow(() -> new IllegalStateException("Role not found"));
        if ("STUDENT".equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException("Cannot create staff member with STUDENT role");
        }
        User newUser = new User();
        newUser.setUsername(request.username());
        newUser.setEmail(request.email());
        String hashedPassword = passwordEncoder.encode(request.password());
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(role);
        newUser.setStatus("ACTIVE");
        newUser.setAuthProvider("EMAIL");
        User saved = userRepository.save(newUser);
        Staff staff = new Staff();
        staff.setUser(saved);
        staff.setCanAccessDashboard(request.canAccessDashboard() != null ? request.canAccessDashboard() : false);
        staffRepository.save(staff);
        userPreferenceService.createDefaultForUser(saved.getId());
        notificationSettingService.createDefaultsForUser(saved.getId());
        financialAccountRepository.findByUserId(saved.getId())
                .orElseGet(() -> {
                    FinancialAccount newAccount = new FinancialAccount();
                    newAccount.setUser(saved);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setPaymentMethod("TASK_BASED");
                    newAccount.setCurrency("EGP");
                    newAccount.setStatus("ACTIVE");
                    return financialAccountRepository.save(newAccount);
                });
        return userAdminService.mapToUserAdminResponse(saved);
    }
    @Transactional
    public TaskPaymentResponse createTask(TaskCreateRequest request, User adminUser) {
        User staffUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (staffUser.getRole() == null || "STUDENT".equalsIgnoreCase(staffUser.getRole().getName())) {
            throw new IllegalStateException("User is not a staff member");
        }
        FinancialAccount account = financialAccountRepository.findByUserId(request.userId())
                .orElseGet(() -> {
                    FinancialAccount newAccount = new FinancialAccount();
                    newAccount.setUser(staffUser);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setPaymentMethod("TASK_BASED");
                    newAccount.setCurrency("EGP");
                    newAccount.setStatus("ACTIVE");
                    return financialAccountRepository.save(newAccount);
                });
        TaskPayment task = new TaskPayment();
        task.setAccount(account);
        task.setTaskTitle(request.taskTitle());
        task.setTaskDescription(request.taskDescription());
        task.setAmount(request.amount() != null ? request.amount() : BigDecimal.ZERO);
        task.setCurrency(account.getCurrency());
        task.setTaskDate(request.taskDate());
        task.setPaymentMonth(request.taskDate().withDayOfMonth(1));
        task.setStatus("PENDING");
        task.setNotes(request.notes());
        task.setExternalTaskId(request.externalTaskId());
        TaskPayment saved = taskPaymentRepository.save(task);
        return mapToTaskPaymentResponse(saved);
    }
    @Transactional
    public TaskPaymentResponse approveTask(UUID taskId, TaskApprovalRequest request, User adminUser) {
        TaskPayment task = taskPaymentRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found"));
        if ("APPROVED".equals(request.status())) {
            task.setStatus("APPROVED");
            task.setApprovedBy(adminUser);
            task.setApprovedAt(LocalDateTime.now());
            task.setRejectionReason(null);
            if ("UNDER_REVIEW".equals(task.getCompletionStatus())) {
                task.setCompletionStatus("COMPLETED");
            }
            if (task.getAmount() != null && task.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                FinancialAccount account = task.getAccount();
                financialService.createTransaction(
                        account,
                        "TASK_PAYMENT",
                        task.getAmount(),
                        "Task payment: " + task.getTaskTitle(),
                        task.getId(),
                        "TASK_PAYMENT"
                );
            }
        } else if ("REJECTED".equals(request.status())) {
            task.setStatus("REJECTED");
            task.setApprovedBy(adminUser);
            task.setApprovedAt(LocalDateTime.now());
            task.setRejectionReason(request.rejectionReason());
            if ("UNDER_REVIEW".equals(task.getCompletionStatus())) {
                task.setCompletionStatus("PENDING");
            }
        }
        if (request.notes() != null && !request.notes().isEmpty()) {
            task.setNotes(request.notes());
        }
        TaskPayment saved = taskPaymentRepository.save(task);
        return mapToTaskPaymentResponse(saved);
    }
    @Transactional
    public PaymentRequestResponse approvePaymentRequest(UUID requestId, PaymentRequestApprovalRequest request, User adminUser) {
        PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalStateException("Payment request not found"));
        if ("APPROVED".equals(request.status())) {
            paymentRequest.setStatus("APPROVED");
            paymentRequest.setReviewedBy(adminUser);
            paymentRequest.setReviewedAt(LocalDateTime.now());
            paymentRequest.setRejectionReason(null);
        } else if ("REJECTED".equals(request.status())) {
            paymentRequest.setStatus("REJECTED");
            paymentRequest.setReviewedBy(adminUser);
            paymentRequest.setReviewedAt(LocalDateTime.now());
            paymentRequest.setRejectionReason(request.rejectionReason());
        } else if ("COMPLETED".equals(request.status())) {
            paymentRequest.setStatus("COMPLETED");
            paymentRequest.setReviewedBy(adminUser);
            paymentRequest.setReviewedAt(LocalDateTime.now());
            paymentRequest.setCompletedAt(LocalDateTime.now());
        }
        PaymentRequest saved = paymentRequestRepository.save(paymentRequest);
        return mapToPaymentRequestResponse(saved);
    }
    @Transactional
    public FinancialTransactionResponse adjustStaffAccount(UUID userId, FinancialAdjustmentRequest request, User adminUser) {
        User staffUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (staffUser.getRole() == null || "STUDENT".equalsIgnoreCase(staffUser.getRole().getName())) {
            throw new IllegalStateException("User is not a staff member");
        }
        FinancialAccount account = financialAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FinancialAccount newAccount = new FinancialAccount();
                    newAccount.setUser(staffUser);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setPaymentMethod("TASK_BASED");
                    newAccount.setCurrency("EGP");
                    newAccount.setStatus("ACTIVE");
                    return financialAccountRepository.save(newAccount);
                });
        FinancialTransaction transaction = financialService.createTransaction(
                account,
                request.transactionType(),
                request.amount(),
                request.description(),
                null,
                "MANUAL_ADJUSTMENT"
        );
        return mapToTransactionResponse(transaction);
    }
    @Transactional(readOnly = true)
    public List<PaymentRequestResponse> getAllPaymentRequests(String status) {
        List<PaymentRequest> requests;
        if (status != null && !status.isEmpty()) {
            requests = paymentRequestRepository.findByStatus(status);
        } else {
            requests = paymentRequestRepository.findAll();
        }
        return requests.stream()
                .filter(pr -> {
                    User user = pr.getAccount().getUser();
                    return user.getRole() != null && !"STUDENT".equalsIgnoreCase(user.getRole().getName());
                })
                .map(this::mapToPaymentRequestResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<TaskPaymentResponse> getAllTasks(String status) {
        List<TaskPayment> tasks = taskPaymentRepository.findAll();
        return tasks.stream()
                .filter(tp -> {
                    User user = tp.getAccount().getUser();
                    return user.getRole() != null && !"STUDENT".equalsIgnoreCase(user.getRole().getName());
                })
                .filter(tp -> status == null || status.isEmpty() || status.equals(tp.getStatus()))
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<TaskPaymentResponse> getCompletedTasksForReview() {
        List<TaskPayment> tasks = taskPaymentRepository.findAll();
        return tasks.stream()
                .filter(tp -> {
                    User user = tp.getAccount().getUser();
                    return user.getRole() != null && !"STUDENT".equalsIgnoreCase(user.getRole().getName());
                })
                .filter(tp -> "UNDER_REVIEW".equals(tp.getCompletionStatus()))
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
    }
    private FinancialAccountResponse mapToAccountResponse(FinancialAccount account) {
        return new FinancialAccountResponse(
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
                account.getNotes()
        );
    }
    private PaymentRequestResponse mapToPaymentRequestResponse(PaymentRequest request) {
        return new PaymentRequestResponse(
                request.getId(),
                request.getAccount().getId(),
                request.getRequestType(),
                request.getAmount(),
                request.getCurrency(),
                request.getReason(),
                request.getStatus(),
                request.getRejectionReason(),
                request.getReviewedBy() != null ? request.getReviewedBy().getId() : null,
                request.getReviewedBy() != null ? request.getReviewedBy().getEmail() : null,
                request.getReviewedAt(),
                request.getCompletedAt(),
                request.getPaymentMethod(),
                request.getPaymentDetails(),
                request.getCreatedAt(),
                request.getUpdatedAt()
        );
    }
    private com.loyalixa.backend.financial.dto.FinancialTransactionResponse mapToTransactionResponse(
            com.loyalixa.backend.financial.FinancialTransaction transaction) {
        return new com.loyalixa.backend.financial.dto.FinancialTransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getTransactionType(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getCurrency(),
                transaction.getDescription(),
                transaction.getReferenceId(),
                transaction.getReferenceType(),
                transaction.getStatus(),
                transaction.getDiscountReason(),
                transaction.getNotes(),
                transaction.getCreatedAt()
        );
    }
    @Transactional
    public FinancialAccountResponse updateEmploymentInfo(UUID userId, EmploymentInfoUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalStateException("User is not a staff member");
        }
        FinancialAccount account = financialAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FinancialAccount newAccount = new FinancialAccount();
                    newAccount.setUser(user);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setPaymentMethod("TASK_BASED");
                    newAccount.setCurrency("EGP");
                    newAccount.setStatus("ACTIVE");
                    return financialAccountRepository.save(newAccount);
                });
        if (request.employmentType() != null) account.setEmploymentType(request.employmentType());
        if (request.hasFixedSalary() != null) account.setHasFixedSalary(request.hasFixedSalary());
        if (request.monthlySalary() != null) account.setMonthlySalary(request.monthlySalary());
        if (request.salaryCurrency() != null) account.setSalaryCurrency(request.salaryCurrency());
        if (request.salaryPaymentDay() != null) account.setSalaryPaymentDay(request.salaryPaymentDay());
        if (request.employmentStartDate() != null) account.setEmploymentStartDate(request.employmentStartDate());
        if (request.employmentEndDate() != null) account.setEmploymentEndDate(request.employmentEndDate());
        if (request.workSchedule() != null) account.setWorkSchedule(request.workSchedule());
        if (request.hoursPerWeek() != null) account.setHoursPerWeek(request.hoursPerWeek());
        if (request.workInstructions() != null) account.setWorkInstructions(request.workInstructions());
        if (request.paymentMethod() != null && !request.paymentMethod().isBlank()) {
            String normalizedMethod = request.paymentMethod().trim().toUpperCase();
            Set<String> allowedMethods = Set.of("FIXED_SALARY", "TASK_BASED", "HYBRID");
            if (!allowedMethods.contains(normalizedMethod)) {
                throw new IllegalArgumentException("Invalid payment method: " + request.paymentMethod());
            }
            account.setPaymentMethod(normalizedMethod);
        }
        FinancialAccount saved = financialAccountRepository.save(account);
        return mapToAccountResponse(saved);
    }
    @Transactional
    public FinancialAccountResponse updateFinancialAccount(UUID userId, FinancialAccountUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
        if (user.getRole() == null || "STUDENT".equalsIgnoreCase(user.getRole().getName())) {
            throw new IllegalStateException("User is not a staff member");
        }
        FinancialAccount account = financialAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    FinancialAccount newAccount = new FinancialAccount();
                    newAccount.setUser(user);
                    newAccount.setBalance(BigDecimal.ZERO);
                    newAccount.setPaymentMethod("TASK_BASED");
                    newAccount.setCurrency("EGP");
                    newAccount.setStatus("ACTIVE");
                    return financialAccountRepository.save(newAccount);
                });
        if (request.bankName() != null) account.setBankName(request.bankName());
        if (request.bankAccountNumber() != null) account.setBankAccountNumber(request.bankAccountNumber());
        if (request.bankIban() != null) account.setBankIban(request.bankIban());
        if (request.bankSwiftCode() != null) account.setBankSwiftCode(request.bankSwiftCode());
        if (request.walletType() != null) account.setWalletType(request.walletType());
        if (request.walletNumber() != null) account.setWalletNumber(request.walletNumber());
        if (request.cardType() != null) account.setCardType(request.cardType());
        if (request.cardNumber() != null) account.setCardNumber(request.cardNumber());
        if (request.cardHolderName() != null) account.setCardHolderName(request.cardHolderName());
        if (request.cardCountry() != null) account.setCardCountry(request.cardCountry());
        if (request.cardBankName() != null) account.setCardBankName(request.cardBankName());
        if (request.cardExpiryDate() != null) account.setCardExpiryDate(request.cardExpiryDate());
        if (request.notes() != null) account.setNotes(request.notes());
        FinancialAccount saved = financialAccountRepository.save(account);
        return mapToAccountResponse(saved);
    }
    private TaskPaymentResponse mapToTaskPaymentResponse(TaskPayment payment) {
        return new TaskPaymentResponse(
                payment.getId(),
                payment.getAccount().getId(),
                payment.getAccount().getUser().getId(),
                payment.getAccount().getUser().getUsername(),
                payment.getAccount().getUser().getEmail(),
                payment.getTaskTitle(),
                payment.getTaskDescription(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getTaskDate(),
                payment.getPaymentMonth(),
                payment.getStatus(),
                payment.getApprovedBy() != null ? payment.getApprovedBy().getId() : null,
                payment.getApprovedBy() != null ? payment.getApprovedBy().getEmail() : null,
                payment.getApprovedAt(),
                payment.getRejectionReason(),
                payment.getNotes(),
                payment.getExternalTaskId(),
                payment.getCompletionStatus(),
                payment.getCompletedAt(),
                payment.getCompletionNotes(),
                payment.getAttachments(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
