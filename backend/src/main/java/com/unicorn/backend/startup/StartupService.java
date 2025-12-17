package com.unicorn.backend.startup;

import com.unicorn.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.unicorn.backend.user.UserRepository;

/**
 * Service class for managing startup operations.
 */
@Service
@RequiredArgsConstructor
public class StartupService {

    private final StartupRepository startupRepository;
    private final UserRepository userRepository;

    /**
     * Create a new startup for the authenticated user.
     *
     * @param request the startup creation request
     * @param owner   the authenticated user (owner)
     * @return the created startup response
     */
    @Transactional
    public StartupResponse createStartup(CreateStartupRequest request, User user) {
        User owner = user;
        if (request.ownerId() != null && user.getRole().contains("ADMIN")) {
            // Admin can assign owner
            owner = userRepository.findById(request.ownerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found with ID: " + request.ownerId()));
        }

        Startup startup = Startup.builder()
                .name(request.name())
                .tagline(request.tagline())
                .fullDescription(request.fullDescription())
                .industry(request.industry())
                .stage(request.stage())
                .fundingGoal(request.fundingGoal())
                .raisedAmount(BigDecimal.ZERO)
                .websiteUrl(request.websiteUrl())
                .logoUrl(request.logoUrl())
                .coverUrl(request.coverUrl())
                .pitchDeckUrl(request.pitchDeckUrl())
                .financialDocumentsUrl(request.financialDocumentsUrl())
                .businessPlanUrl(request.businessPlanUrl())
                .businessModelUrl(request.businessModelUrl())
                .facebookUrl(request.facebookUrl())
                .instagramUrl(request.instagramUrl())
                .twitterUrl(request.twitterUrl())
                .ownerRole(request.ownerRole())
                // Status defaults to APPROVED via @Builder.Default
                .owner(owner) // Temporarily use 'user', will fix owner lookup in next step
                .build();

        Startup savedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(savedStartup);
    }

    /**
     * Get all startups owned by the authenticated user.
     *
     * @param owner the authenticated user
     * @return list of startup responses
     */
    @Transactional(readOnly = true)
    public List<StartupResponse> getMyStartups(User owner) {
        return startupRepository.findAllByOwner(owner)
                .stream()
                .map(StartupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public StartupResponse updateStartup(UUID id, UpdateStartupRequest request, User user) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        boolean isAdmin = user.getRole().contains("ADMIN");
        // Validate ownership
        if (!isAdmin && !startup.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to update this startup");
        }

        // Update fields if provided in request
        if (request.name() != null)
            startup.setName(request.name());
        if (request.tagline() != null)
            startup.setTagline(request.tagline());
        if (request.fullDescription() != null)
            startup.setFullDescription(request.fullDescription());
        if (request.industry() != null)
            startup.setIndustry(request.industry());
        if (request.stage() != null)
            startup.setStage(request.stage());
        if (request.fundingGoal() != null)
            startup.setFundingGoal(request.fundingGoal());
        if (request.raisedAmount() != null)
            startup.setRaisedAmount(request.raisedAmount());
        if (request.websiteUrl() != null)
            startup.setWebsiteUrl(request.websiteUrl());
        if (request.logoUrl() != null)
            startup.setLogoUrl(request.logoUrl());
        if (request.coverUrl() != null)
            startup.setCoverUrl(request.coverUrl());
        if (request.pitchDeckUrl() != null)
            startup.setPitchDeckUrl(request.pitchDeckUrl());
        if (request.financialDocumentsUrl() != null)
            startup.setFinancialDocumentsUrl(request.financialDocumentsUrl());
        if (request.businessPlanUrl() != null)
            startup.setBusinessPlanUrl(request.businessPlanUrl());
        if (request.businessModelUrl() != null)
            startup.setBusinessModelUrl(request.businessModelUrl());
        if (request.facebookUrl() != null)
            startup.setFacebookUrl(request.facebookUrl());
        if (request.instagramUrl() != null)
            startup.setInstagramUrl(request.instagramUrl());
        if (request.twitterUrl() != null)
            startup.setTwitterUrl(request.twitterUrl());

        Startup updatedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(updatedStartup);
    }

    /**
     * Delete a startup.
     * Validates that the authenticated user is the owner.
     *
     * @param id   the startup ID
     * @param user the authenticated user
     * @throws IllegalArgumentException if startup not found
     * @throws AccessDeniedException    if user is not the owner
     */
    @Transactional
    public void deleteStartup(UUID id, User user) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Validate ownership
        if (!startup.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have permission to delete this startup");
        }

        startupRepository.delete(startup);
    }

    /**
     * Get all startups with a specific status.
     * For admin use.
     *
     * @param status the status to filter by
     * @return list of startup responses
     */
    @Transactional(readOnly = true)
    public List<StartupResponse> getStartupsByStatus(StartupStatus status) {
        return startupRepository.findAllByStatus(status)
                .stream()
                .map(StartupResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update startup status (Admin only).
     * Allows admin to approve or reject startup applications.
     *
     * @param id     the startup ID
     * @param status the new status
     * @return the updated startup response
     * @throws IllegalArgumentException if startup not found
     */
    @Transactional
    public StartupResponse updateStartupStatus(UUID id, StartupStatus status) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        startup.setStatus(status);
        Startup updatedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(updatedStartup);
    }

    /**
     * Transfer startup ownership to a new user (Admin only).
     *
     * @param id       the startup ID
     * @param newOwner the new owner user
     * @return the updated startup response
     * @throws IllegalArgumentException if startup not found
     */
    @Transactional
    public StartupResponse transferOwnership(UUID id, User newOwner) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found: " + id));

        // TODO: Create audit log entry when audit service is implemented
        // auditLogService.log("OWNERSHIP_TRANSFER", startup.getId(),
        // startup.getOwner().getId(), newOwner.getId());

        startup.setOwner(newOwner);
        Startup updatedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(updatedStartup);
    }

    /**
     * Get all startups with advanced filtration.
     *
     * @param filter   the filter criteria
     * @param pageable pagination parameters
     * @return page of startup responses
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<StartupResponse> getStartups(
            StartupFilterRequest filter,
            org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Startup> spec = StartupSpecification
                .buildSpecification(filter);
        return startupRepository.findAll(spec, pageable)
                .map(StartupResponse::fromEntity);
    }

    /**
     * Get all startups with pagination (Admin only).
     *
     * @param pageable pagination parameters
     * @return page of startup responses
     */
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<StartupResponse> getAllStartups(
            org.springframework.data.domain.Pageable pageable) {
        return startupRepository.findAll(pageable)
                .map(StartupResponse::fromEntity);
    }

    /**
     * Get a startup by ID.
     *
     * @param id the startup ID
     * @return the startup response
     * @throws IllegalArgumentException if startup not found
     */
    @Transactional(readOnly = true)
    public StartupResponse getStartupById(UUID id) {
        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found: " + id));
        return StartupResponse.fromEntity(startup);
    }
}
