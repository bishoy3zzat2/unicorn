package com.loyalixa.backend.subscription;
import com.loyalixa.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {
    @Query("SELECT us FROM UserSubscription us WHERE us.user.id = :userId AND us.status = 'ACTIVE' AND (us.endDate IS NULL OR us.endDate > :now) ORDER BY us.startDate DESC")
    Optional<UserSubscription> findActiveSubscriptionByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
    List<UserSubscription> findByUserIdOrderByStartDateDesc(UUID userId);
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND (us.endDate IS NULL OR us.endDate > :now)")
    List<UserSubscription> findAllActiveSubscriptions(@Param("now") LocalDateTime now);
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate IS NOT NULL AND us.endDate BETWEEN :now AND :futureDate")
    List<UserSubscription> findSubscriptionsExpiringSoon(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);
    @Query("SELECT us FROM UserSubscription us WHERE us.status = 'ACTIVE' AND us.endDate IS NOT NULL AND us.endDate < :now")
    List<UserSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);
    @Query("SELECT COUNT(us) FROM UserSubscription us WHERE us.plan.id = :planId AND us.status = 'ACTIVE' AND (us.endDate IS NULL OR us.endDate > :now)")
    Long countActiveSubscriptionsByPlanId(@Param("planId") UUID planId, @Param("now") LocalDateTime now);
    @Query("SELECT us FROM UserSubscription us WHERE us.plan.id = :planId AND us.status = 'ACTIVE' AND (us.endDate IS NULL OR us.endDate > :now)")
    List<UserSubscription> findActiveSubscriptionsByPlanId(@Param("planId") UUID planId, @Param("now") LocalDateTime now);
    @Modifying
    @Query("UPDATE UserSubscription us SET us.status = 'EXPIRED' WHERE us.status = 'ACTIVE' AND us.endDate IS NOT NULL AND us.endDate < :now")
    int expireSubscriptions(@Param("now") LocalDateTime now);
}
