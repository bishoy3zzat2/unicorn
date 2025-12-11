package com.loyalixa.backend.financial;
import com.loyalixa.backend.financial.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class FinancialService {
    private final FinancialAccountRepository financialAccountRepository;
    private final FinancialTransactionRepository financialTransactionRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final TaskPaymentRepository taskPaymentRepository;
    private final SalaryPaymentRepository salaryPaymentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    @org.springframework.beans.factory.annotation.Value("${file.upload.base-dir:uploads}")
    private String uploadBaseDir;
    @org.springframework.beans.factory.annotation.Value("${file.upload.tasks-dir:tasks}")
    private String tasksUploadDir;
    @org.springframework.beans.factory.annotation.Value("${file.upload.max-size:10485760}")
    private long maxFileSize;
    @org.springframework.beans.factory.annotation.Value("${file.upload.tasks.allowed-extensions:pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,gif,zip,rar}")
    private String allowedExtensionsStr;
    private List<String> allowedExtensions;
    public FinancialService(
            FinancialAccountRepository financialAccountRepository,
            FinancialTransactionRepository financialTransactionRepository,
            PaymentRequestRepository paymentRequestRepository,
            TaskPaymentRepository taskPaymentRepository,
            SalaryPaymentRepository salaryPaymentRepository,
            UserRepository userRepository) {
        this.financialAccountRepository = financialAccountRepository;
        this.financialTransactionRepository = financialTransactionRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.taskPaymentRepository = taskPaymentRepository;
        this.salaryPaymentRepository = salaryPaymentRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();
    }
    @PostConstruct
    private void init() {
        this.allowedExtensions = java.util.Arrays.asList(allowedExtensionsStr.toLowerCase().split(","));
        try {
            Path uploadPath = Paths.get(uploadBaseDir, tasksUploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }
    private String getUploadDir() {
        return uploadBaseDir + "/" + tasksUploadDir + "/";
    }
    @Transactional
    public FinancialAccount getOrCreateAccount(User user) {
        Optional<FinancialAccount> accountOpt = financialAccountRepository.findByUserId(user.getId());
        if (accountOpt.isPresent()) {
            return accountOpt.get();
        }
        FinancialAccount account = new FinancialAccount();
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setPaymentMethod("TASK_BASED");  
        account.setCurrency("EGP");
        account.setStatus("ACTIVE");
        return financialAccountRepository.save(account);
    }
    @Transactional(readOnly = true)
    public FinancialAccountResponse getAccount(UUID userId) {
        FinancialAccount account = financialAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Financial account not found"));
        return mapToAccountResponse(account);
    }
    @Transactional
    public PaymentRequestResponse createPaymentRequest(UUID userId, PaymentRequestCreateRequest request) {
        FinancialAccount account = getOrCreateAccount(userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found")));
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAccount(account);
        paymentRequest.setRequestType(request.requestType());
        paymentRequest.setAmount(request.amount());
        paymentRequest.setCurrency(account.getCurrency());
        paymentRequest.setReason(request.reason());
        paymentRequest.setStatus("PENDING");
        paymentRequest.setPaymentMethod(request.paymentMethod());
        paymentRequest.setPaymentDetails(request.paymentDetails());
        PaymentRequest saved = paymentRequestRepository.save(paymentRequest);
        createTransaction(account, "ADVANCE_REQUEST".equals(request.requestType()) ? "ADVANCE_REQUEST" : "WITHDRAWAL_REQUEST",
                request.amount(), "Payment request created: " + request.reason(), saved.getId(), "PAYMENT_REQUEST");
        return mapToPaymentRequestResponse(saved);
    }
    @Transactional(readOnly = true)
    public List<PaymentRequestResponse> getPaymentRequests(UUID userId, String status) {
        List<PaymentRequest> requests;
        if (status != null && !status.isEmpty()) {
            requests = paymentRequestRepository.findByUserIdAndStatus(userId, status);
        } else {
            requests = paymentRequestRepository.findByUserId(userId);
        }
        return requests.stream()
                .map(this::mapToPaymentRequestResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> getTransactions(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<FinancialTransaction> transactions = financialTransactionRepository
                .findByAccountIdOrderByCreatedAtDesc(
                        financialAccountRepository.findByUserId(userId)
                                .orElseThrow(() -> new IllegalStateException("Account not found"))
                                .getId(),
                        pageable);
        return transactions.getContent().stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<TaskPaymentResponse> getTaskPayments(UUID userId, LocalDate month) {
        List<TaskPayment> payments;
        if (month != null) {
            payments = taskPaymentRepository.findByUserIdAndMonth(userId, month);
        } else {
            payments = taskPaymentRepository.findByUserId(userId);
        }
        return payments.stream()
                .map(this::mapToTaskPaymentResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<SalaryPaymentResponse> getSalaryPayments(UUID userId) {
        List<SalaryPayment> payments = salaryPaymentRepository.findByUserId(userId);
        return payments.stream()
                .map(this::mapToSalaryPaymentResponse)
                .collect(Collectors.toList());
    }
    @Transactional
    public FinancialTransaction createTransaction(FinancialAccount account, String type, 
                                                  BigDecimal amount, String description,
                                                  UUID referenceId, String referenceType) {
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter;
        if (type.contains("DEPOSIT") || type.contains("SALARY") || type.contains("TASK") || type.contains("BONUS")) {
            balanceAfter = balanceBefore.add(amount);
        } else {
            balanceAfter = balanceBefore.subtract(amount);
        }
        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setCurrency(account.getCurrency());
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transaction.setStatus("COMPLETED");
        account.setBalance(balanceAfter);
        financialAccountRepository.save(account);
        return financialTransactionRepository.save(transaction);
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
    private FinancialTransactionResponse mapToTransactionResponse(FinancialTransaction transaction) {
        return new FinancialTransactionResponse(
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
    private SalaryPaymentResponse mapToSalaryPaymentResponse(SalaryPayment payment) {
        return new SalaryPaymentResponse(
                payment.getId(),
                payment.getAccount().getId(),
                payment.getPaymentMonth(),
                payment.getBaseSalary(),
                payment.getBonus(),
                payment.getDeductions(),
                payment.getTotalAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaidAt(),
                payment.getApprovedBy() != null ? payment.getApprovedBy().getId() : null,
                payment.getApprovedBy() != null ? payment.getApprovedBy().getEmail() : null,
                payment.getApprovedAt(),
                payment.getDeductionDetails(),
                payment.getNotes(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
    @Transactional
    public TaskPaymentResponse completeTask(UUID taskId, UUID userId, TaskCompletionRequest request) {
        TaskPayment task = taskPaymentRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found"));
        if (!task.getAccount().getUser().getId().equals(userId)) {
            throw new IllegalStateException("Task does not belong to user");
        }
        if ("COMPLETED".equals(task.getCompletionStatus()) || "UNDER_REVIEW".equals(task.getCompletionStatus())) {
            throw new IllegalStateException("Task is already completed or under review");
        }
        task.setCompletionStatus("UNDER_REVIEW");
        task.setCompletedAt(LocalDateTime.now());
        task.setCompletionNotes(request.completionNotes());
        TaskPayment saved = taskPaymentRepository.save(task);
        return mapToTaskPaymentResponse(saved);
    }
    @Transactional
    public TaskPaymentResponse uploadTaskAttachments(UUID taskId, UUID userId, List<MultipartFile> files) {
        TaskPayment task = taskPaymentRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException("Task not found"));
        if (!task.getAccount().getUser().getId().equals(userId)) {
            throw new IllegalStateException("Task does not belong to user");
        }
        List<Map<String, Object>> attachments = new ArrayList<>();
        if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
            try {
                attachments = objectMapper.readValue(task.getAttachments(), 
                    new TypeReference<List<Map<String, Object>>>() {});
            } catch (Exception e) {
                attachments = new ArrayList<>();
            }
        }
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            if (file.getSize() > maxFileSize) {
                throw new IllegalStateException("File size exceeds maximum allowed size: " + 
                    (maxFileSize / 1024 / 1024) + "MB");
            }
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalStateException("File name is required");
            }
            String extension = "";
            int lastDot = originalFilename.lastIndexOf('.');
            if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
                extension = originalFilename.substring(lastDot + 1).toLowerCase();
            }
            if (!allowedExtensions.contains(extension)) {
                throw new IllegalStateException("File type not allowed. Allowed types: " + 
                    String.join(", ", allowedExtensions));
            }
            try {
                String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
                Path filePath = Paths.get(getUploadDir() + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                Map<String, Object> attachment = new HashMap<>();
                attachment.put("url", "/api/v1/files/tasks/" + fileName);
                attachment.put("name", originalFilename);
                attachment.put("size", file.getSize());
                attachment.put("type", file.getContentType());
                attachment.put("extension", extension);
                attachment.put("uploadedAt", LocalDateTime.now().toString());
                attachment.put("filePath", filePath.toString());
                attachments.add(attachment);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to upload file: " + e.getMessage());
            }
        }
        try {
            task.setAttachments(objectMapper.writeValueAsString(attachments));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to save attachments: " + e.getMessage());
        }
        TaskPayment saved = taskPaymentRepository.save(task);
        return mapToTaskPaymentResponse(saved);
    }
}
