package com.unicorn.backend.startup;

import com.unicorn.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Startup entity.
 */
@Repository
public interface StartupRepository extends JpaRepository<Startup, UUID> {

    /**
     * Find all startups owned by a specific user.
     *
     * @param owner the user who owns the startups
     * @return list of startups owned by the user
     */
    List<Startup> findAllByOwner(User owner);

    /**
     * Find all startups with a specific status.
     *
     * @param status the startup status
     * @return list of startups with the given status
     */
    List<Startup> findAllByStatus(StartupStatus status);

    /**
     * Find a startup by ID and owner.
     * Useful for ownership validation.
     *
     * @param id    the startup ID
     * @param owner the owner user
     * @return Optional containing the startup if found
     */
    Optional<Startup> findByIdAndOwner(UUID id, User owner);
}
