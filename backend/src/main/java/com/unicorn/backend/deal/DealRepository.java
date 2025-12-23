package com.unicorn.backend.deal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Deal entity operations.
 */
@Repository
public interface DealRepository extends JpaRepository<Deal, UUID> {

    /**
     * Find all deals for a specific investor.
     */
    List<Deal> findByInvestorIdOrderByDealDateDesc(UUID investorId);

    /**
     * Find all deals for a specific startup.
     */
    List<Deal> findByStartupIdOrderByDealDateDesc(UUID startupId);

    /**
     * Find all deals with pagination.
     */
    Page<Deal> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Search deals by investor name, startup name, or notes.
     */
    @Query("SELECT d FROM Deal d " +
            "WHERE LOWER(d.investor.firstName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.investor.lastName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.investor.email) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.startup.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY d.createdAt DESC")
    Page<Deal> searchDeals(@Param("query") String query, Pageable pageable);

    /**
     * Count deals by status.
     */
    long countByStatus(DealStatus status);

    /**
     * Get total amount of completed deals.
     */
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM Deal d WHERE d.status = 'COMPLETED'")
    java.math.BigDecimal getTotalCompletedDealsAmount();

    /**
     * Get total commission revenue from completed deals.
     * Calculation: SUM(amount * commissionPercentage / 100)
     */
    @Query("SELECT COALESCE(SUM(d.amount * d.commissionPercentage / 100), 0) FROM Deal d WHERE d.status = 'COMPLETED'")
    java.math.BigDecimal getTotalCommissionRevenue();
}
