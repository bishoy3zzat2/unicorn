package com.loyalixa.backend.financial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {
    List<FinancialTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Page<FinancialTransaction> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
    @Query("SELECT ft FROM FinancialTransaction ft WHERE ft.account.user.id = :userId ORDER BY ft.createdAt DESC")
    List<FinancialTransaction> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT ft FROM FinancialTransaction ft WHERE ft.account.user.id = :userId AND ft.transactionType = :type ORDER BY ft.createdAt DESC")
    List<FinancialTransaction> findByUserIdAndType(@Param("userId") UUID userId, @Param("type") String type);
    @Query("SELECT ft FROM FinancialTransaction ft WHERE ft.account.user.id = :userId AND ft.createdAt BETWEEN :startDate AND :endDate ORDER BY ft.createdAt DESC")
    List<FinancialTransaction> findByUserIdAndDateRange(@Param("userId") UUID userId, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);
}
