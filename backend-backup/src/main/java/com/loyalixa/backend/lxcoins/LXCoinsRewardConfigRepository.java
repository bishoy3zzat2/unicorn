package com.loyalixa.backend.lxcoins;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface LXCoinsRewardConfigRepository extends JpaRepository<LXCoinsRewardConfig, Long> {
    Optional<LXCoinsRewardConfig> findByActivityType(String activityType);
    List<LXCoinsRewardConfig> findByIsEnabledTrue();
}
