package com.loyalixa.backend.lxcoins;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface LXCoinsTransactionRepository extends JpaRepository<LXCoinsTransaction, UUID> {
    List<LXCoinsTransaction> findByAccountUserId(UUID userId);
    Page<LXCoinsTransaction> findByAccountUserId(UUID userId, Pageable pageable);
    @Query("SELECT t FROM LXCoinsTransaction t WHERE t.account.user.id = :userId AND t.transactionType = :transactionType")
    List<LXCoinsTransaction> findByUserIdAndTransactionType(@Param("userId") UUID userId, @Param("transactionType") String transactionType);
}
