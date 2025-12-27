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
    private final StartupMemberRepository startupMemberRepository;
    private final com.unicorn.backend.appconfig.AppConfigService appConfigService;

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

        if (!"ACTIVE".equalsIgnoreCase(owner.getStatus())) {
            throw new AccessDeniedException("User must be ACTIVE to own a startup.");
        }

        // Validate lengths
        int maxName = appConfigService.getIntValue("max_startup_name_length", 100);
        if (request.name() != null && request.name().length() > maxName) {
            throw new IllegalArgumentException("Startup name must not exceed " + maxName + " characters");
        }

        int maxTagline = appConfigService.getIntValue("max_tagline_length", 150);
        if (request.tagline() != null && request.tagline().length() > maxTagline) {
            throw new IllegalArgumentException("Tagline must not exceed " + maxTagline + " characters");
        }

        int maxAbout = appConfigService.getIntValue("max_about_length", 2000);
        if (request.fullDescription() != null && request.fullDescription().length() > maxAbout) {
            throw new IllegalArgumentException("Description must not exceed " + maxAbout + " characters");
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
                // Status defaults to ACTIVE via @Builder.Default
                .owner(owner)
                .build();

        // Ensure owner is added as a member
        String initialRole = request.ownerRole() != null ? request.ownerRole().name() : "FOUNDER";
        StartupMember ownerMember = new StartupMember(startup, owner, initialRole, java.time.LocalDateTime.now());
        startup.getMembers().add(ownerMember);

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

        if (!isAdmin && !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new AccessDeniedException("Your account is not ACTIVE.");
        }

        // Update fields if provided in request
        if (request.name() != null) {
            int maxName = appConfigService.getIntValue("max_startup_name_length", 100);
            if (request.name().length() > maxName) {
                throw new IllegalArgumentException("Startup name must not exceed " + maxName + " characters");
            }
            startup.setName(request.name());
        }
        if (request.tagline() != null) {
            int maxTagline = appConfigService.getIntValue("max_tagline_length", 150);
            if (request.tagline().length() > maxTagline) {
                throw new IllegalArgumentException("Tagline must not exceed " + maxTagline + " characters");
            }
            startup.setTagline(request.tagline());
        }
        if (request.fullDescription() != null) {
            int maxAbout = appConfigService.getIntValue("max_about_length", 2000);
            if (request.fullDescription().length() > maxAbout) {
                throw new IllegalArgumentException("Description must not exceed " + maxAbout + " characters");
            }
            startup.setFullDescription(request.fullDescription());
        }
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
     * Transfer startup ownership to a new user.
     *
     * @param id           the startup ID
     * @param newOwner     the new owner user
     * @param newOwnerRole the role for the new owner (e.g. FOUNDER, CEO)
     * @return the updated startup response
     * @throws IllegalArgumentException if startup not found
     */
    @Transactional
    public StartupResponse transferOwnership(UUID id, User newOwner, String newOwnerRole) {
        if (!"ACTIVE".equalsIgnoreCase(newOwner.getStatus())) {
            throw new IllegalArgumentException("New owner must be ACTIVE.");
        }

        Startup startup = startupRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found: " + id));

        User oldOwner = startup.getOwner();

        // Ensure old owner remains as a member (if not already)
        boolean isOldOwnerMember = startup.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(oldOwner.getId()));

        if (!isOldOwnerMember) {
            String oldRole = startup.getOwnerRole() != null ? startup.getOwnerRole().name() : "FOUNDER";
            java.time.LocalDateTime joinedAt = startup.getCreatedAt() != null ? startup.getCreatedAt()
                    : java.time.LocalDateTime.now();
            StartupMember oldMember = new StartupMember(startup, oldOwner, oldRole, joinedAt);
            startup.getMembers().add(oldMember);
        }

        // Add new owner as member if not already a member
        boolean isNewOwnerMember = startup.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(newOwner.getId()) && m.isActive());

        String roleToUse = (newOwnerRole != null && !newOwnerRole.isEmpty()) ? newOwnerRole : "FOUNDER";

        if (!isNewOwnerMember) {
            // Add new owner as member with the specified role
            StartupMember newMember = new StartupMember(startup, newOwner, roleToUse, java.time.LocalDateTime.now());
            startup.getMembers().add(newMember);
        } else {
            // Update existing member's role if provided
            startup.getMembers().stream()
                    .filter(m -> m.getUser().getId().equals(newOwner.getId()) && m.isActive())
                    .findFirst()
                    .ifPresent(member -> member.setRole(roleToUse));
        }

        // Set owner role from the provided role
        try {
            StartupRole role = StartupRole.valueOf(roleToUse);
            startup.setOwnerRole(role);
        } catch (IllegalArgumentException e) {
            startup.setOwnerRole(StartupRole.OTHER);
        }

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

    /**
     * Add a member to the startup.
     */
    @Transactional
    public StartupResponse addMember(UUID startupId, UUID userId, String role, java.time.LocalDateTime joinedAt,
            java.time.LocalDateTime leftAt,
            User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate requester is Owner or Admin
        boolean isAdmin = requester.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwner().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or admin can add members.");
        }

        if (!"ACTIVE".equalsIgnoreCase(userToAdd.getStatus())) {
            throw new IllegalArgumentException("Cannot add a user who is not ACTIVE.");
        }

        // Check if already a member
        boolean alreadyMember = startup.getMembers().stream()
                .anyMatch(m -> m.getUser().getId().equals(userId) && m.isActive());

        if (alreadyMember) {
            throw new IllegalArgumentException("User is already an active member of this startup.");
        }

        StartupMember member = new StartupMember(startup, userToAdd, role, joinedAt);
        member.setLeftAt(leftAt);
        if (leftAt != null && leftAt.isBefore(java.time.LocalDateTime.now())) {
            member.setActive(false);
        }
        startup.getMembers().add(member);

        Startup savedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(savedStartup);
    }

    /**
     * Update a member's role in the startup.
     */
    @Transactional
    public StartupResponse updateMemberRole(UUID startupId, UUID memberUserId, String newRole, User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Validate requester is Owner or Admin
        boolean isAdmin = requester.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwner().getId().equals(requester.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or admin can update member roles.");
        }

        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(memberUserId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not an active member of this startup"));

        member.setRole(newRole);

        // If this member is the owner, also update ownerRole
        if (startup.getOwner().getId().equals(memberUserId)) {
            try {
                StartupRole role = StartupRole.valueOf(newRole);
                startup.setOwnerRole(role);
            } catch (IllegalArgumentException e) {
                startup.setOwnerRole(StartupRole.OTHER);
            }
        }

        Startup savedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(savedStartup);
    }

    /**
     * Leave a startup (Soft Delete).
     *
     * @param startupId the startup ID
     * @param user      the authenticated user
     */
    @Transactional
    public void leaveStartup(UUID startupId, User user) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not an active member of this startup"));

        member.setLeftAt(java.time.LocalDateTime.now());
        member.setActive(false);
        startupRepository.save(startup);
    }

    /**
     * Reactivate a member who has left the startup.
     */
    @Transactional
    public StartupResponse reactivateMember(UUID startupId, UUID memberUserId, User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Validate requester is Owner or Admin
        boolean isAdmin = requester.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwner().getId().equals(requester.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or admin can reactivate members.");
        }

        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(memberUserId) && !m.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User is not an inactive member of this startup"));

        member.setActive(true);
        member.setLeftAt(null);

        Startup savedStartup = startupRepository.save(startup);
        return StartupResponse.fromEntity(savedStartup);
    }

    /**
     * Unsign from a startup (Hard Delete).
     *
     * @param startupId the startup ID
     * @param user      the authenticated user
     */
    @Transactional
    public void unsignStartup(UUID startupId, User user) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Find match regardless of active status
        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this startup"));

        startup.getMembers().remove(member);
        startupMemberRepository.delete(member);
        startupRepository.save(startup);
    }

    /**
     * Remove a member (Soft Delete) - Admin/Owner Action.
     */
    @Transactional
    public void removeMember(UUID startupId, UUID memberUserId, User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Validate requester is Owner or Admin
        boolean isAdmin = requester.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwner().getId().equals(requester.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or admin can remove members.");
        }

        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(memberUserId) && m.isActive())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target user is not an active member of this startup"));

        // Prevent removing the owner via this method (should use transfer ownership)
        if (member.getUser().getId().equals(startup.getOwner().getId())) {
            throw new IllegalArgumentException("Cannot remove the owner. Transfer ownership first.");
        }

        member.setLeftAt(java.time.LocalDateTime.now());
        member.setActive(false);
        startupRepository.save(startup);
    }

    /**
     * Delete a member (Hard Delete) - Admin/Owner Action.
     */
    @Transactional
    public void deleteMember(UUID startupId, UUID memberUserId, User requester) {
        Startup startup = startupRepository.findById(startupId)
                .orElseThrow(() -> new IllegalArgumentException("Startup not found"));

        // Validate requester is Owner or Admin
        boolean isAdmin = requester.getRole().contains("ADMIN");
        boolean isOwner = startup.getOwner().getId().equals(requester.getId());
        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Only the owner or admin can delete members.");
        }

        StartupMember member = startup.getMembers().stream()
                .filter(m -> m.getUser().getId().equals(memberUserId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this startup"));

        // Prevent deleting the owner
        if (member.getUser().getId().equals(startup.getOwner().getId())) {
            throw new IllegalArgumentException("Cannot delete the owner. Transfer ownership first.");
        }

        startup.getMembers().remove(member);
        startupMemberRepository.delete(member);
        startupRepository.save(startup);
    }
}
