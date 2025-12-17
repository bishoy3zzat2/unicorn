package com.unicorn.backend.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllActiveByUserId(UUID userId); // Note: custom query might be needed if "active"
                                                           // impliesExpiry check logic here or in service

    // Actually, JPA method names:
    List<RefreshToken> findByUserId(UUID userId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT r.user) FROM RefreshToken r WHERE r.expiryDate > :date")
    long countDistinctUserByExpiryDateAfter(
            @org.springframework.data.repository.query.Param("date") java.time.Instant date);

    long countByExpiryDateAfter(java.time.Instant date);

    long countByExpiryDateBefore(java.time.Instant date);

    List<RefreshToken> findAllByExpiryDateAfter(java.time.Instant date);

    @Modifying
    void deleteByToken(String token);

    @Modifying
    void deleteByUserId(UUID userId);
}
