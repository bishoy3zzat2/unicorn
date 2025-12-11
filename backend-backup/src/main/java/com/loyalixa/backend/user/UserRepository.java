package com.loyalixa.backend.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.staff WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    long countByStatus(String status);
    List<User> findByStatus(String status);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") String role);
    @Query("SELECT COUNT(u) FROM User u WHERE u.role.id = :roleId")
    long countByRoleId(@Param("roleId") UUID roleId);
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.role.id = :roleId")
    List<User> findByRoleId(@Param("roleId") UUID roleId);
    @Query("SELECT DISTINCT u FROM User u JOIN FETCH u.role r LEFT JOIN FETCH r.permissions")
    List<User> findAllUsersWithRolesAndPermissions();
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.role r LEFT JOIN FETCH r.permissions LEFT JOIN FETCH u.staff WHERE u.id = :id")
    Optional<User> findByIdWithStaff(@Param("id") UUID id);
    List<User> findByAppealRequestedTrueAndAppealStatus(String appealStatus);
    @Query(value = "SELECT * FROM users WHERE status = 'SUSPENDED' AND suspension_type = 'TEMPORARY' AND suspended_until IS NOT NULL AND suspended_until <= :now", nativeQuery = true)
    List<User> findUsersWithExpiredSuspensions(@Param("now") LocalDateTime now);
    @Query(value = "SELECT * FROM users WHERE status = 'BANNED' AND ban_type = 'TEMPORARY' AND banned_until IS NOT NULL AND banned_until <= :now", nativeQuery = true)
    List<User> findUsersWithExpiredBans(@Param("now") LocalDateTime now);
    @Query("SELECT u FROM User u WHERE u.status = 'SUSPENDED' AND u.suspensionType = 'TEMPORARY' AND u.suspendedUntil IS NOT NULL")
    List<User> findAllTemporarySuspendedUsers();
    @Query("SELECT u FROM User u WHERE u.status = 'BANNED' AND u.banType = 'TEMPORARY' AND u.bannedUntil IS NOT NULL")
    List<User> findAllTemporaryBannedUsers();
}