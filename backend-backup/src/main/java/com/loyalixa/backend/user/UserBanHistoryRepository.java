package com.loyalixa.backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface UserBanHistoryRepository extends JpaRepository<UserBanHistory, UUID> {
    @EntityGraph(attributePaths = {"performedBy"})
    List<UserBanHistory> findByUserIdOrderByActionAtDesc(UUID userId);
    @Query("SELECT COUNT(h) FROM UserBanHistory h WHERE h.user.id = :userId AND h.action = 'BAN'")
    long countBansByUserId(@Param("userId") UUID userId);
}
