package com.unicorn.backend.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Payment entity operations.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

        /**
         * Find all payments for a user ordered by timestamp descending.
         */
        Page<Payment> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

        /**
         * Find recent payments for a user (top 10).
         */
        List<Payment> findTop10ByUserIdOrderByTimestampDesc(UUID userId);

        /**
         * Find payments by status.
         */
        List<Payment> findByStatus(PaymentStatus status);

        /**
         * Find payments in a date range.
         */
        @Query("SELECT p FROM Payment p WHERE p.timestamp BETWEEN :startDate AND :endDate ORDER BY p.timestamp DESC")
        List<Payment> findByTimestampBetween(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get total revenue for a period (completed payments only).
         */
        @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p " +
                        "WHERE p.status = 'COMPLETED' AND p.timestamp BETWEEN :startDate AND :endDate")
        BigDecimal getTotalRevenueForPeriod(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get total revenue for a period grouped by currency (completed payments only).
         */
        @Query("SELECT p.currency, COALESCE(SUM(p.amount), 0) FROM Payment p " +
                        "WHERE p.status = 'COMPLETED' AND p.timestamp BETWEEN :startDate AND :endDate " +
                        "GROUP BY p.currency")
        List<Object[]> getTotalRevenueForPeriodByCurrency(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Get monthly revenue data for charts.
         */
        /**
         * Get monthly revenue data grouped by currency.
         */
        @Query(value = "SELECT TO_CHAR(p.timestamp, 'Mon') as month, " +
                        "EXTRACT(MONTH FROM p.timestamp) as month_num, " +
                        "p.currency, " +
                        "COALESCE(SUM(p.amount), 0) as revenue " +
                        "FROM payments p " +
                        "WHERE p.status = 'COMPLETED' AND p.timestamp >= :startDate " +
                        "GROUP BY TO_CHAR(p.timestamp, 'Mon'), EXTRACT(MONTH FROM p.timestamp), p.currency " +
                        "ORDER BY month_num", nativeQuery = true)
        List<Object[]> getMonthlyRevenueByCurrency(@Param("startDate") LocalDateTime startDate);

        /**
         * Count payments by status.
         */
        long countByStatus(PaymentStatus status);

        /**
         * Get recent payments.
         */
        Page<Payment> findAllByOrderByTimestampDesc(Pageable pageable);
}
