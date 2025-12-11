package com.loyalixa.backend.lxcoins;
import com.loyalixa.backend.lxcoins.dto.*;
import com.loyalixa.backend.subscription.SubscriptionService;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class LXCoinsService {
    private final LXCoinsAccountRepository accountRepository;
    private final LXCoinsTransactionRepository transactionRepository;
    private final LXCoinsRewardConfigRepository rewardConfigRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    public LXCoinsService(LXCoinsAccountRepository accountRepository,
                         LXCoinsTransactionRepository transactionRepository,
                         LXCoinsRewardConfigRepository rewardConfigRepository,
                         UserRepository userRepository,
                         SubscriptionService subscriptionService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.rewardConfigRepository = rewardConfigRepository;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }
    @Transactional
    public LXCoinsAccount getOrCreateAccount(User user) {
        return accountRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    LXCoinsAccount account = new LXCoinsAccount();
                    account.setUser(user);
                    account.setBalance(BigDecimal.ZERO);
                    account.setTotalEarned(BigDecimal.ZERO);
                    account.setTotalSpent(BigDecimal.ZERO);
                    account.setIsActive(false);  
                    return accountRepository.save(account);
                });
    }
    @Transactional(readOnly = true)
    public LXCoinsAccountResponse getAccountByUserId(UUID userId) {
        LXCoinsAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("LXCoins account not found"));
        return mapToAccountResponse(account);
    }
    @Transactional
    public LXCoinsTransaction createTransaction(LXCoinsAccount account, String transactionType,
                                               BigDecimal amount, String description,
                                               UUID referenceId, String referenceType, User processedBy) {
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter;
        boolean isEarning = transactionType.contains("EARNED") || 
                           transactionType.contains("ADJUSTMENT") && amount.compareTo(BigDecimal.ZERO) > 0 ||
                           transactionType.contains("REFUND");
        if (isEarning) {
            balanceAfter = balanceBefore.add(amount);
            account.setTotalEarned(account.getTotalEarned().add(amount));
        } else {
            if (balanceBefore.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient LXCoins balance.");
            }
            balanceAfter = balanceBefore.subtract(amount);
            account.setTotalSpent(account.getTotalSpent().add(amount));
        }
        LXCoinsTransaction transaction = new LXCoinsTransaction();
        transaction.setAccount(account);
        transaction.setTransactionType(transactionType);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        transaction.setProcessedBy(processedBy);
        account.setBalance(balanceAfter);
        accountRepository.save(account);
        return transactionRepository.save(transaction);
    }
    @Transactional
    public LXCoinsTransaction awardCoins(UUID userId, String activityType, UUID referenceId, String referenceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LXCoinsAccount account = getOrCreateAccount(user);
        if (account.getIsActive() == null || !account.getIsActive()) {
            throw new IllegalStateException("LXCoins account is inactive. Subscribe to a paid plan to activate your account.");
        }
        BigDecimal rewardAmount = BigDecimal.ZERO;
        BigDecimal subscriptionReward = subscriptionService.getCoinsRewardForActivity(userId, activityType);
        if (subscriptionReward.compareTo(BigDecimal.ZERO) > 0) {
            rewardAmount = subscriptionReward;
        } else {
            Optional<LXCoinsRewardConfig> configOpt = rewardConfigRepository.findByActivityType(activityType);
            if (configOpt.isPresent()) {
                LXCoinsRewardConfig config = configOpt.get();
                if (!config.getIsEnabled()) {
                    throw new IllegalStateException("Reward is disabled for activity: " + activityType);
                }
                rewardAmount = config.getBaseReward();
            } else {
                throw new IllegalStateException("Reward configuration not found for activity: " + activityType);
            }
        }
        if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException("Reward amount must be greater than zero");
        }
        String description = "Earned " + rewardAmount + " LXCoins for " + activityType;
        return createTransaction(account, "EARNED_" + activityType, rewardAmount, description, referenceId, referenceType, null);
    }
    @Transactional
    public LXCoinsTransaction spendCoins(UUID userId, BigDecimal amount, String description, UUID referenceId, String referenceType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LXCoinsAccount account = getOrCreateAccount(user);
        return createTransaction(account, "SPENT_PRODUCT_PURCHASE", amount, description, referenceId, referenceType, null);
    }
    @Transactional
    public LXCoinsTransaction adjustCoins(LXCoinsAdjustmentRequest request, User adminUser) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        LXCoinsAccount account = getOrCreateAccount(user);
        String transactionType = request.transactionType() != null ? request.transactionType() : "ADMIN_ADJUSTMENT";
        String description = request.description() != null ? request.description() : "Admin adjustment";
        return createTransaction(account, transactionType, request.amount(), description, null, "ADMIN", adminUser);
    }
    @Transactional(readOnly = true)
    public Page<LXCoinsTransactionResponse> getTransactionHistory(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByAccountUserId(userId, pageable)
                .map(this::mapToTransactionResponse);
    }
    @Transactional(readOnly = true)
    public List<LXCoinsRewardConfigResponse> getAllRewardConfigs() {
        return rewardConfigRepository.findAll().stream()
                .map(this::mapToRewardConfigResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public LXCoinsRewardConfigResponse getRewardConfig(String activityType) {
        LXCoinsRewardConfig config = rewardConfigRepository.findByActivityType(activityType)
                .orElseThrow(() -> new IllegalArgumentException("Reward configuration not found"));
        return mapToRewardConfigResponse(config);
    }
    @Transactional
    public LXCoinsRewardConfigResponse saveRewardConfig(LXCoinsRewardConfigRequest request) {
        Optional<LXCoinsRewardConfig> existing = rewardConfigRepository.findByActivityType(request.activityType());
        LXCoinsRewardConfig config;
        if (existing.isPresent()) {
            config = existing.get();
            config.setBaseReward(request.baseReward());
            config.setIsEnabled(request.isEnabled() != null ? request.isEnabled() : true);
            config.setConfigJson(request.configJson());
            config.setDescription(request.description());
        } else {
            config = new LXCoinsRewardConfig();
            config.setActivityType(request.activityType());
            config.setBaseReward(request.baseReward());
            config.setIsEnabled(request.isEnabled() != null ? request.isEnabled() : true);
            config.setConfigJson(request.configJson());
            config.setDescription(request.description());
        }
        LXCoinsRewardConfig saved = rewardConfigRepository.save(config);
        return mapToRewardConfigResponse(saved);
    }
    private LXCoinsAccountResponse mapToAccountResponse(LXCoinsAccount account) {
        return new LXCoinsAccountResponse(
                account.getId(), account.getUser().getId(),
                account.getUser().getEmail(), account.getUser().getUsername(),
                account.getBalance(), account.getTotalEarned(), account.getTotalSpent(),
                account.getIsActive() != null ? account.getIsActive() : false,
                account.getCreatedAt(), account.getUpdatedAt()
        );
    }
    private LXCoinsTransactionResponse mapToTransactionResponse(LXCoinsTransaction transaction) {
        return new LXCoinsTransactionResponse(
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
    }
    private LXCoinsRewardConfigResponse mapToRewardConfigResponse(LXCoinsRewardConfig config) {
        return new LXCoinsRewardConfigResponse(
                config.getId(), config.getActivityType(), config.getBaseReward(),
                config.getIsEnabled(), config.getConfigJson(), config.getDescription(),
                config.getCreatedAt(), config.getUpdatedAt()
        );
    }
}
