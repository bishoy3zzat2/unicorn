package com.loyalixa.backend.financial;
import com.loyalixa.backend.financial.dto.*;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/financial")
public class FinancialController {
    private final FinancialService financialService;
    public FinancialController(FinancialService financialService) {
        this.financialService = financialService;
    }
    @GetMapping("/account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FinancialAccountResponse> getAccount(
            @AuthenticationPrincipal User currentUser
    ) {
        FinancialAccountResponse account = financialService.getAccount(currentUser.getId());
        return ResponseEntity.ok(account);
    }
    @PostMapping("/payment-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaymentRequestResponse> createPaymentRequest(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PaymentRequestCreateRequest request
    ) {
        PaymentRequestResponse response = financialService.createPaymentRequest(
                currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/payment-requests")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentRequestResponse>> getPaymentRequests(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String status
    ) {
        List<PaymentRequestResponse> requests = financialService.getPaymentRequests(
                currentUser.getId(), status);
        return ResponseEntity.ok(requests);
    }
    @GetMapping("/transactions")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FinancialTransactionResponse>> getTransactions(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        List<FinancialTransactionResponse> transactions = financialService.getTransactions(
                currentUser.getId(), page, size);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/task-payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TaskPaymentResponse>> getTaskPayments(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month
    ) {
        List<TaskPaymentResponse> payments = financialService.getTaskPayments(
                currentUser.getId(), month);
        return ResponseEntity.ok(payments);
    }
    @GetMapping("/salary-payments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<SalaryPaymentResponse>> getSalaryPayments(
            @AuthenticationPrincipal User currentUser
    ) {
        List<SalaryPaymentResponse> payments = financialService.getSalaryPayments(
                currentUser.getId());
        return ResponseEntity.ok(payments);
    }
    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskPaymentResponse> completeTask(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody TaskCompletionRequest request
    ) {
        TaskPaymentResponse task = financialService.completeTask(
                taskId, currentUser.getId(), request);
        return ResponseEntity.ok(task);
    }
    @PostMapping("/tasks/{taskId}/attachments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TaskPaymentResponse> uploadTaskAttachments(
            @PathVariable UUID taskId,
            @AuthenticationPrincipal User currentUser,
            @RequestParam("files") List<org.springframework.web.multipart.MultipartFile> files
    ) {
        TaskPaymentResponse task = financialService.uploadTaskAttachments(
                taskId, currentUser.getId(), files);
        return ResponseEntity.ok(task);
    }
}
