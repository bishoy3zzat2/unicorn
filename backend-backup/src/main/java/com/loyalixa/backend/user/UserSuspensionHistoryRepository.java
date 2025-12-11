package com.loyalixa.backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface UserSuspensionHistoryRepository extends JpaRepository<UserSuspensionHistory, UUID> {
    @EntityGraph(attributePaths = {"performedBy"})
    List<UserSuspensionHistory> findByUserIdOrderByActionAtDesc(UUID userId);
    @Query("SELECT COUNT(h) FROM UserSuspensionHistory h WHERE h.user.id = :userId AND h.action = 'SUSPEND'")
    long countSuspensionsByUserId(@Param("userId") UUID userId);
}
