package com.unicorn.backend.investor;

import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for InvestorProfile entity.
 */
@Repository
public interface InvestorProfileRepository extends JpaRepository<InvestorProfile, UUID> {

    /**
     * Find an investor profile by user.
     *
     * @param user the user associated with the profile
     * @return Optional containing the profile if found
     */
    Optional<InvestorProfile> findByUser(User user);

    /**
     * Check if a profile exists for a specific user.
     *
     * @param user the user to check
     * @return true if profile exists, false otherwise
     */
    boolean existsByUser(User user);
}
