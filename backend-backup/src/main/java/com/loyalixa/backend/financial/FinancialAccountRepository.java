package com.loyalixa.backend.financial;
import com.loyalixa.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, UUID> {
    Optional<FinancialAccount> findByUser(User user);
    Optional<FinancialAccount> findByUserId(UUID userId);
    @Query("SELECT fa FROM FinancialAccount fa WHERE fa.user.id = :userId")
    Optional<FinancialAccount> findByUserIdWithUser(@Param("userId") UUID userId);
}
