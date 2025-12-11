package com.loyalixa.backend.subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByCode(String code);
    Optional<SubscriptionPlan> findByName(String name);
    List<SubscriptionPlan> findByIsActiveTrueOrderByDisplayOrderAsc();
    List<SubscriptionPlan> findByPlanType(String planType);
    Optional<SubscriptionPlan> findByIsDefaultTrue();
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
