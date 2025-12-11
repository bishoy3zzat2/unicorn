package com.loyalixa.backend.financial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SalaryPaymentRepository extends JpaRepository<SalaryPayment, UUID> {
    List<SalaryPayment> findByAccountIdOrderByPaymentMonthDesc(UUID accountId);
    Page<SalaryPayment> findByAccountIdOrderByPaymentMonthDesc(UUID accountId, Pageable pageable);
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.account.user.id = :userId ORDER BY sp.paymentMonth DESC")
    List<SalaryPayment> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.account.user.id = :userId AND sp.paymentMonth = :month")
    Optional<SalaryPayment> findByUserIdAndMonth(@Param("userId") UUID userId, @Param("month") LocalDate month);
    @Query("SELECT sp FROM SalaryPayment sp WHERE sp.account.user.id = :userId AND sp.status = :status ORDER BY sp.paymentMonth DESC")
    List<SalaryPayment> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
}
