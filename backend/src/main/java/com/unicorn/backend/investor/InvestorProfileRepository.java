package com.unicorn.backend.investor;

import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for InvestorProfile entity.
 */
@Repository
public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, UUID> {

        /**
         * Find an investor profile by user.
         *
         * @param user the user associated with the profile
         * @return Optional containing the profile if found
         */
        Optional<InvestorProfile> findByUser(User user);

        /**
         * Check if a profile exists for a specific user.
         *
         * @param user the user to check
         * @return true if profile exists, false otherwise
         */
        boolean existsByUser(User user);

        /**
         * Count investors with isVerified = false.
         */
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(i.id) FROM InvestorProfile i WHERE i.isVerified = false")
        long countPendingVerifications();

        /**
         * Find investors who requested verification but are not yet verified.
         * Only includes users with ACTIVE status.
         */
        @org.springframework.data.jpa.repository.Query("SELECT i FROM InvestorProfile i WHERE i.verificationRequested = true AND i.isVerified = false AND i.user.status = 'ACTIVE' ORDER BY i.verificationRequestedAt ASC")
        java.util.List<InvestorProfile> findPendingVerificationQueue();

        /**
         * Find all verified investors.
         */
        @org.springframework.data.jpa.repository.Query("SELECT i FROM InvestorProfile i WHERE i.isVerified = true")
        java.util.List<InvestorProfile> findVerifiedInvestors();

        /**
         * Count investors with isVerified = true.
         */
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(i.id) FROM InvestorProfile i WHERE i.isVerified = true")
        long countVerifiedInvestors();

        /**
         * Sum of investment budget across all profiles.
         */
        @org.springframework.data.jpa.repository.Query("SELECT SUM(i.investmentBudget) FROM InvestorProfile i")
        java.math.BigDecimal sumInvestmentBudget();

        /**
         * Find investors pending verification with pagination and optional search.
         * Only includes users with ACTIVE status.
         */
        @org.springframework.data.jpa.repository.Query("SELECT i FROM InvestorProfile i WHERE i.verificationRequested = true AND i.isVerified = false "
                        + "AND i.user.status = 'ACTIVE' "
                        + "AND (:query IS NULL OR :query = '' OR LOWER(i.user.email) LIKE LOWER(CONCAT('%', :query, '%'))) "
                        + "ORDER BY i.verificationRequestedAt ASC")
        org.springframework.data.domain.Page<InvestorProfile> findPendingVerificationQueuePaginated(
                        @org.springframework.data.repository.query.Param("query") String query,
                        org.springframework.data.domain.Pageable pageable);

        /**
         * Count pending verification requests (only ACTIVE users).
         */
        @org.springframework.data.jpa.repository.Query("SELECT COUNT(i) FROM InvestorProfile i WHERE i.verificationRequested = true AND i.isVerified = false AND i.user.status = 'ACTIVE'")
        long countPendingQueue();
}
