package com.loyalixa.backend.financial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Repository
public interface TaskPaymentRepository extends JpaRepository<TaskPayment, UUID> {
    List<TaskPayment> findByAccountIdOrderByTaskDateDesc(UUID accountId);
    Page<TaskPayment> findByAccountIdOrderByTaskDateDesc(UUID accountId, Pageable pageable);
    @Query("SELECT tp FROM TaskPayment tp WHERE tp.account.user.id = :userId ORDER BY tp.taskDate DESC")
    List<TaskPayment> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT tp FROM TaskPayment tp WHERE tp.account.user.id = :userId AND tp.paymentMonth = :month ORDER BY tp.taskDate DESC")
    List<TaskPayment> findByUserIdAndMonth(@Param("userId") UUID userId, @Param("month") LocalDate month);
    @Query("SELECT tp FROM TaskPayment tp WHERE tp.account.user.id = :userId AND tp.status = :status ORDER BY tp.taskDate DESC")
    List<TaskPayment> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
}
