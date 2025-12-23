package com.unicorn.backend.deal;

import com.unicorn.backend.startup.Startup;
import com.unicorn.backend.startup.StartupRepository;
import com.unicorn.backend.user.User;
import com.unicorn.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing investment deals.
 */
@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository dealRepository;
    private final UserRepository userRepository;
    private final StartupRepository startupRepository;

    /**
     * Create a new deal.
     */
    @Transactional
    public DealResponse createDeal(DealRequest request) {
        User investor = userRepository.findById(request.getInvestorId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Investor not found with ID: " + request.getInvestorId()));

        // Verify user is an investor
        if (!"INVESTOR".equalsIgnoreCase(investor.getRole())) {
            throw new IllegalArgumentException("User is not an investor");
        }

        Startup startup = startupRepository.findById(request.getStartupId())
                .orElseThrow(
                        () -> new IllegalArgumentException("Startup not found with ID: " + request.getStartupId()));

        Deal deal = Deal.builder()
                .investor(investor)
                .startup(startup)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "USD")
                .status(request.getStatus() != null ? request.getStatus() : DealStatus.PENDING)
                .dealType(request.getDealType())
                .equityPercentage(request.getEquityPercentage())
                .commissionPercentage(request.getCommissionPercentage())
                .notes(request.getNotes())
                .dealDate(request.getDealDate() != null ? request.getDealDate() : LocalDateTime.now())
                .build();

        Deal savedDeal = dealRepository.save(deal);
        return DealResponse.fromEntity(savedDeal);
    }

    /**
     * Get a deal by ID.
     */
    @Transactional(readOnly = true)
    public DealResponse getDealById(UUID id) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found with ID: " + id));
        return DealResponse.fromEntity(deal);
    }

    /**
     * Get all deals for an investor.
     */
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForInvestor(UUID investorId) {
        return dealRepository.findByInvestorIdOrderByDealDateDesc(investorId)
                .stream()
                .map(DealResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all deals for a startup.
     */
    @Transactional(readOnly = true)
    public List<DealResponse> getDealsForStartup(UUID startupId) {
        return dealRepository.findByStartupIdOrderByDealDateDesc(startupId)
                .stream()
                .map(DealResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all deals with pagination and optional search.
     */
    @Transactional(readOnly = true)
    public Page<DealResponse> getAllDeals(Pageable pageable, String query) {
        Page<Deal> deals;
        if (query != null && !query.trim().isEmpty()) {
            deals = dealRepository.searchDeals(query.trim(), pageable);
        } else {
            deals = dealRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return deals.map(DealResponse::fromEntity);
    }

    /**
     * Update a deal.
     */
    @Transactional
    public DealResponse updateDeal(UUID id, DealRequest request) {
        Deal deal = dealRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Deal not found with ID: " + id));

        if (request.getInvestorId() != null && !request.getInvestorId().equals(deal.getInvestor().getId())) {
            User investor = userRepository.findById(request.getInvestorId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Investor not found with ID: " + request.getInvestorId()));
            if (!"INVESTOR".equalsIgnoreCase(investor.getRole())) {
                throw new IllegalArgumentException("User is not an investor");
            }
            deal.setInvestor(investor);
        }

        if (request.getStartupId() != null && !request.getStartupId().equals(deal.getStartup().getId())) {
            Startup startup = startupRepository.findById(request.getStartupId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Startup not found with ID: " + request.getStartupId()));
            deal.setStartup(startup);
        }

        if (request.getAmount() != null) {
            deal.setAmount(request.getAmount());
        }
        if (request.getCurrency() != null) {
            deal.setCurrency(request.getCurrency());
        }
        if (request.getStatus() != null) {
            deal.setStatus(request.getStatus());
        }
        if (request.getDealType() != null) {
            deal.setDealType(request.getDealType());
        }
        if (request.getEquityPercentage() != null) {
            deal.setEquityPercentage(request.getEquityPercentage());
        }
        if (request.getCommissionPercentage() != null) {
            deal.setCommissionPercentage(request.getCommissionPercentage());
        }
        if (request.getNotes() != null) {
            deal.setNotes(request.getNotes());
        }
        if (request.getDealDate() != null) {
            deal.setDealDate(request.getDealDate());
        }

        Deal updatedDeal = dealRepository.save(deal);
        return DealResponse.fromEntity(updatedDeal);
    }

    /**
     * Delete a deal.
     */
    @Transactional
    public void deleteDeal(UUID id) {
        if (!dealRepository.existsById(id)) {
            throw new IllegalArgumentException("Deal not found with ID: " + id);
        }
        dealRepository.deleteById(id);
    }

    /**
     * Get deal statistics.
     */
    @Transactional(readOnly = true)
    public DealStats getDealStats() {
        return DealStats.builder()
                .totalDeals(dealRepository.count())
                .pendingDeals(dealRepository.countByStatus(DealStatus.PENDING))
                .completedDeals(dealRepository.countByStatus(DealStatus.COMPLETED))
                .cancelledDeals(dealRepository.countByStatus(DealStatus.CANCELLED))
                .totalCompletedAmount(dealRepository.getTotalCompletedDealsAmount())
                .totalCommissionRevenue(dealRepository.getTotalCommissionRevenue())
                .build();
    }
}
