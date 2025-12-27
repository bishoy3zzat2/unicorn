package com.unicorn.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE lower(u.email) LIKE lower(concat('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchUsers(String query,
            org.springframework.data.domain.Pageable pageable);

    long countByStatus(String status);

    long countByRole(String role);

    long countByRoleAndInvestorProfile_IsVerifiedTrue(String role);

    long countByCreatedAtAfter(java.time.LocalDateTime date);

    /**
     * Find users by role (for announcements).
     */
    List<User> findByRole(String role);

    /**
     * Find users by status (for announcements).
     */
    List<User> findByStatus(String status);
}
