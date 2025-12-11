package com.unicorn.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE lower(u.email) LIKE lower(concat('%', :query, '%'))")
    org.springframework.data.domain.Page<User> searchUsers(String query,
            org.springframework.data.domain.Pageable pageable);
}
