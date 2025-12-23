package com.unicorn.backend.chat;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for MonthlyMessageLimit entity operations.
 * Tracks Elite startup monthly messaging limits per investor.
 */
@Repository
public interface MonthlyMessageLimitRepository extends JpaRepository<MonthlyMessageLimit, UUID> {

    /**
     * Check if a startup has already messaged a specific investor in a given month.
     * Returns true if the limit has been used.
     */
    boolean existsByStartupAndInvestorAndMonthAndYear(
            Startup startup,
            User investor,
            Integer month,
            Integer year);
}
