package com.loyalixa.backend.financial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    List<PaymentRequest> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    Page<PaymentRequest> findByAccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.account.user.id = :userId ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.account.user.id = :userId AND pr.status = :status ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") String status);
    @Query("SELECT pr FROM PaymentRequest pr WHERE pr.status = :status ORDER BY pr.createdAt DESC")
    List<PaymentRequest> findByStatus(@Param("status") String status);
}
