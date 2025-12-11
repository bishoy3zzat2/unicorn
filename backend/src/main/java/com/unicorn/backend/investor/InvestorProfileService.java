package com.unicorn.backend.investor;

import com.unicorn.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing investor profile operations.
 */
@Service
@RequiredArgsConstructor
public class InvestorProfileService {

    private final InvestorProfileRepository investorProfileRepository;

    /**
     * Create or update an investor profile for the authenticated user.
     *
     * @param request the profile request
     * @param user    the authenticated user
     * @return the created/updated profile response
     */
    @Transactional
    public InvestorProfileResponse createOrUpdateProfile(CreateInvestorProfileRequest request, User user) {
        InvestorProfile profile = investorProfileRepository.findByUser(user)
                .orElse(InvestorProfile.builder().user(user).build());

        // Update fields
        profile.setInvestmentBudget(request.investmentBudget());
        profile.setBio(request.bio());
        profile.setPreferredIndustries(request.preferredIndustries());
        profile.setLinkedInUrl(request.linkedInUrl());

        InvestorProfile savedProfile = investorProfileRepository.save(profile);
        return InvestorProfileResponse.fromEntity(savedProfile);
    }

    /**
     * Get the investor profile for the authenticated user.
     *
     * @param user the authenticated user
     * @return the profile response, or null if not found
     */
    @Transactional(readOnly = true)
    public InvestorProfileResponse getMyProfile(User user) {
        return investorProfileRepository.findByUser(user)
                .map(InvestorProfileResponse::fromEntity)
                .orElse(null);
    }

    /**
     * Check if the user has an investor profile.
     *
     * @param user the user to check
     * @return true if profile exists
     */
    @Transactional(readOnly = true)
    public boolean hasProfile(User user) {
        return investorProfileRepository.existsByUser(user);
    }
}
