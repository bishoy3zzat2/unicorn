package com.unicorn.backend.admin;

import com.unicorn.backend.investor.InvestorProfileRepository;
import com.unicorn.backend.payment.PaymentService;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.subscription.SubscriptionService;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * Controller for dashboard statistics.
 */
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
@RequiredArgsConstructor
public class StatsController {

    private final UserRepository userRepository;
    private final StartupRepository startupRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentService paymentService;

    /**
     * Get dashboard statistics.
     * 
     * GET /api/v1/admin/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        // Count users
        long totalUsers = userRepository.count();

        // Count active startups
        long activeStartups = startupRepository.countByStatus(
                com.unicorn.backend.startup.StartupStatus.ACTIVE);

        // Count investors with profiles
        long activeInvestors = investorProfileRepository.count();

        // Count pending investor verifications
        long pendingVerifications = investorProfileRepository.countPendingVerifications();

        // Calculate MRR
        BigDecimal mrr = subscriptionService.calculateMRR();

        // Calculate total funding raised by startups
        BigDecimal totalFunding = startupRepository.getTotalFundingRaised();

        DashboardStatsResponse response = DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .activeStartups(activeStartups)
                .activeInvestors(activeInvestors)
                .pendingVerifications(pendingVerifications)
                .mrr(mrr != null ? mrr : BigDecimal.ZERO)
                .totalFunding(totalFunding != null ? totalFunding : BigDecimal.ZERO)
                // TODO: Calculate growth percentages based on historical data
                .userGrowth(12.5)
                .startupGrowth(8.3)
                .investorGrowth(5.2)
                .mrrGrowth(15.7)
                .build();

        return ResponseEntity.ok(response);
    }
}
