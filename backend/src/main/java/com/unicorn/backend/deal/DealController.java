package com.unicorn.backend.deal;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing investment deals.
 * All endpoints are admin-only.
 */
@RestController
@RequestMapping("/api/v1/admin/deals")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class DealController {

    private final DealService dealService;

    /**
     * Get all deals with pagination and optional search.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDeals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        Page<DealResponse> deals = dealService.getAllDeals(PageRequest.of(page, size), query);
        return ResponseEntity.ok(Map.of(
                "content", deals.getContent(),
                "totalElements", deals.getTotalElements(),
                "totalPages", deals.getTotalPages(),
                "currentPage", deals.getNumber(),
                "pageSize", deals.getSize()));
    }

    /**
     * Get a deal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DealResponse> getDealById(@PathVariable UUID id) {
        return ResponseEntity.ok(dealService.getDealById(id));
    }

    /**
     * Get all deals for an investor.
     */
    @GetMapping("/investor/{investorId}")
    public ResponseEntity<List<DealResponse>> getDealsForInvestor(@PathVariable UUID investorId) {
        return ResponseEntity.ok(dealService.getDealsForInvestor(investorId));
    }

    /**
     * Get all deals for a startup.
     */
    @GetMapping("/startup/{startupId}")
    public ResponseEntity<List<DealResponse>> getDealsForStartup(@PathVariable UUID startupId) {
        return ResponseEntity.ok(dealService.getDealsForStartup(startupId));
    }

    /**
     * Create a new deal.
     */
    @PostMapping
    public ResponseEntity<DealResponse> createDeal(@Valid @RequestBody DealRequest request) {
        DealResponse deal = dealService.createDeal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(deal);
    }

    /**
     * Update a deal.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DealResponse> updateDeal(
            @PathVariable UUID id,
            @Valid @RequestBody DealRequest request) {
        return ResponseEntity.ok(dealService.updateDeal(id, request));
    }

    /**
     * Delete a deal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteDeal(@PathVariable UUID id) {
        dealService.deleteDeal(id);
        return ResponseEntity.ok(Map.of("message", "Deal deleted successfully"));
    }

    /**
     * Get deal statistics.
     */
    @GetMapping("/stats")
    public ResponseEntity<DealStats> getDealStats() {
        return ResponseEntity.ok(dealService.getDealStats());
    }
}
