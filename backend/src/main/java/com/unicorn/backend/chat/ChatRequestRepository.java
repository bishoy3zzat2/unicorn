package com.unicorn.backend.chat;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ChatRequest entity operations.
 */
@Repository
public interface ChatRequestRepository extends JpaRepository<ChatRequest, UUID> {

    /**
     * Find all pending requests for an investor.
     */
    List<ChatRequest> findByInvestorAndStatusOrderByCreatedAtDesc(User investor, RequestStatus status);

    /**
     * Find a specific request between a startup and investor.
     */
    Optional<ChatRequest> findByStartupAndInvestor(Startup startup, User investor);

    /**
     * Check if a request exists between startup and investor with specific status.
     */
    boolean existsByStartupAndInvestorAndStatus(Startup startup, User investor, RequestStatus status);

    /**
     * Find all requests from a specific startup.
     */
    List<ChatRequest> findByStartupOrderByCreatedAtDesc(Startup startup);
}
