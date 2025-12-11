package com.loyalixa.backend.lxcoins;
import com.loyalixa.backend.lxcoins.dto.*;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/lxcoins")
public class LXCoinsAdminController {
    private final LXCoinsService lxCoinsService;
    public LXCoinsAdminController(LXCoinsService lxCoinsService) {
        this.lxCoinsService = lxCoinsService;
    }
    @GetMapping("/accounts/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:view')")
    public ResponseEntity<LXCoinsAccountResponse> getAccountByUserId(@PathVariable UUID userId) {
        try {
            LXCoinsAccountResponse account = lxCoinsService.getAccountByUserId(userId);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("/adjust")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:adjust')")
    public ResponseEntity<?> adjustCoins(
            @Valid @RequestBody LXCoinsAdjustmentRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            LXCoinsTransaction transaction = lxCoinsService.adjustCoins(request, currentUser);
            LXCoinsTransactionResponse response = new LXCoinsTransactionResponse(
                    transaction.getId(), transaction.getAccount().getId(),
                    transaction.getAccount().getUser().getId(),
                    transaction.getTransactionType(), transaction.getAmount(),
                    transaction.getBalanceBefore(), transaction.getBalanceAfter(),
                    transaction.getDescription(), transaction.getReferenceId(),
                    transaction.getReferenceType(),
                    transaction.getProcessedBy() != null ? transaction.getProcessedBy().getId() : null,
                    transaction.getProcessedBy() != null ? transaction.getProcessedBy().getEmail() : null,
                    transaction.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/transactions/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:view')")
    public ResponseEntity<Page<LXCoinsTransactionResponse>> getTransactionHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<LXCoinsTransactionResponse> transactions = lxCoinsService.getTransactionHistory(userId, page, size);
        return ResponseEntity.ok(transactions);
    }
    @GetMapping("/reward-configs")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:view')")
    public ResponseEntity<List<LXCoinsRewardConfigResponse>> getAllRewardConfigs() {
        List<LXCoinsRewardConfigResponse> configs = lxCoinsService.getAllRewardConfigs();
        return ResponseEntity.ok(configs);
    }
    @GetMapping("/reward-configs/{activityType}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:view')")
    public ResponseEntity<LXCoinsRewardConfigResponse> getRewardConfig(@PathVariable String activityType) {
        try {
            LXCoinsRewardConfigResponse config = lxCoinsService.getRewardConfig(activityType);
            return ResponseEntity.ok(config);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("/reward-configs")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:update')")
    public ResponseEntity<?> saveRewardConfig(@Valid @RequestBody LXCoinsRewardConfigRequest request) {
        try {
            LXCoinsRewardConfigResponse config = lxCoinsService.saveRewardConfig(request);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/reward-configs/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('lxcoins:update')")
    public ResponseEntity<?> updateRewardConfig(
            @PathVariable Long id,
            @Valid @RequestBody LXCoinsRewardConfigRequest request
    ) {
        try {
            LXCoinsRewardConfigResponse config = lxCoinsService.saveRewardConfig(request);
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
