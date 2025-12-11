package com.loyalixa.backend.security;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(UUID userId);
    void deleteByToken(String token);
    void deleteByUserIdAndDeviceId(UUID userId, String deviceId);
    @Deprecated
    Optional<RefreshToken> findByUserId(UUID userId);
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiryDate > :now ORDER BY rt.lastUsedAt DESC NULLS LAST, rt.createdAt DESC")
    List<RefreshToken> findAllActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
    List<RefreshToken> findAllByUserIdOrderByLastUsedAtDescCreatedAtDesc(UUID userId);
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiryDate > :now")
    long countActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
    Optional<RefreshToken> findByUserIdAndDeviceId(UUID userId, String deviceId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM refresh_tokens WHERE id = (SELECT id FROM refresh_tokens WHERE user_id = :userId AND expiry_date > :now ORDER BY COALESCE(last_used_at, '1970-01-01'::timestamp) ASC, created_at ASC LIMIT 1)", nativeQuery = true)
    int deleteOldestActiveTokenByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}