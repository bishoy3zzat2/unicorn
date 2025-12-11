package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.GiftVoucherAdminResponse;
import com.loyalixa.backend.marketing.dto.GiftVoucherCreateRequest;
import com.loyalixa.backend.marketing.dto.GiftVoucherUpdateRequest;
import com.loyalixa.backend.marketing.dto.GiftVoucherSearchRequest;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/admin/gifts")
@PreAuthorize("hasRole('ADMIN')")
public class GiftAdminController {
    private final GiftVoucherRepository giftVoucherRepository;
    private final GiftService giftService;
    public GiftAdminController(GiftVoucherRepository giftVoucherRepository, GiftService giftService) {
        this.giftVoucherRepository = giftVoucherRepository;
        this.giftService = giftService;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('gift:view_all') or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllGiftVouchers(
            @RequestParam(required = false) String voucherCode,
            @RequestParam(required = false) String recipientEmail,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID senderId,
            @RequestParam(required = false) UUID redeemerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issuedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime issuedTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime redeemedFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime redeemedTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "issuedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            Specification<GiftVoucher> spec = buildSpecification(
                    voucherCode, recipientEmail, status, courseId, senderId, redeemerId,
                    issuedFrom, issuedTo, redeemedFrom, redeemedTo
            );
            Sort sort = sortDir.equalsIgnoreCase("ASC") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<GiftVoucher> voucherPage = giftVoucherRepository.findAll(spec, pageable);
            List<GiftVoucherAdminResponse> responses = voucherPage.getContent().stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            var response = new java.util.HashMap<String, Object>();
            response.put("content", responses);
            response.put("totalElements", voucherPage.getTotalElements());
            response.put("totalPages", voucherPage.getTotalPages());
            response.put("currentPage", page);
            response.put("size", size);
            response.put("numberOfElements", voucherPage.getNumberOfElements());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching gift vouchers: " + e.getMessage());
        }
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('gift:view_all') or hasRole('ADMIN')")
    public ResponseEntity<?> getGiftVoucherById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            GiftVoucher voucher = giftVoucherRepository.findByIdWithRelations(id)
                    .orElseThrow(() -> new IllegalArgumentException("Gift voucher not found"));
            return ResponseEntity.ok(toResponse(voucher));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching gift voucher: " + e.getMessage());
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('gift:create') or hasRole('ADMIN')")
    public ResponseEntity<?> createGiftVoucher(
            @Valid @RequestBody GiftVoucherCreateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            GiftVoucher voucher = giftService.createGiftVoucher(request);
            GiftVoucherAdminResponse response = toResponse(
                    giftVoucherRepository.findByIdWithRelations(voucher.getId())
                            .orElseThrow(() -> new IllegalStateException("Failed to fetch created voucher"))
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating gift voucher: " + e.getMessage());
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('gift:update') or hasRole('ADMIN')")
    public ResponseEntity<?> updateGiftVoucher(
            @PathVariable UUID id,
            @Valid @RequestBody GiftVoucherUpdateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            GiftVoucher voucher = giftService.updateGiftVoucher(id, request);
            GiftVoucherAdminResponse response = toResponse(
                    giftVoucherRepository.findByIdWithRelations(voucher.getId())
                            .orElseThrow(() -> new IllegalStateException("Failed to fetch updated voucher"))
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating gift voucher: " + e.getMessage());
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('gift:delete') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteGiftVoucher(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            giftService.deleteGiftVoucher(id);
            return ResponseEntity.ok().body("Gift voucher deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting gift voucher: " + e.getMessage());
        }
    }
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('gift:view_all') or hasRole('ADMIN')")
    public ResponseEntity<?> getGiftVoucherStatistics(@AuthenticationPrincipal User currentUser) {
        try {
            List<GiftVoucher> allVouchers = giftVoucherRepository.findAll();
            long total = allVouchers.size();
            long issued = allVouchers.stream().filter(v -> "ISSUED".equals(v.getStatus())).count();
            long redeemed = allVouchers.stream().filter(v -> "REDEEMED".equals(v.getStatus())).count();
            long expired = allVouchers.stream().filter(v -> "EXPIRED".equals(v.getStatus())).count();
            var stats = new java.util.HashMap<String, Object>();
            stats.put("total", total);
            stats.put("issued", issued);
            stats.put("redeemed", redeemed);
            stats.put("expired", expired);
            stats.put("redemptionRate", total > 0 ? (double) redeemed / total * 100 : 0);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching statistics: " + e.getMessage());
        }
    }
    private Specification<GiftVoucher> buildSpecification(
            String voucherCode, String recipientEmail, String status,
            UUID courseId, UUID senderId, UUID redeemerId,
            LocalDateTime issuedFrom, LocalDateTime issuedTo,
            LocalDateTime redeemedFrom, LocalDateTime redeemedTo
    ) {
        return (root, query, cb) -> {
            if (query != null && query.getResultType() != null && 
                !query.getResultType().equals(Long.class) && 
                !query.getResultType().equals(long.class)) {
                root.fetch("course", JoinType.INNER);
                root.fetch("sender", JoinType.INNER);
                root.fetch("redeemer", JoinType.LEFT);
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (voucherCode != null && !voucherCode.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("voucherCode")), 
                        "%" + voucherCode.toLowerCase() + "%"));
            }
            if (recipientEmail != null && !recipientEmail.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("recipientEmail")), 
                        "%" + recipientEmail.toLowerCase() + "%"));
            }
            if (status != null && !status.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (courseId != null) {
                predicates.add(cb.equal(root.get("course").get("id"), courseId));
            }
            if (senderId != null) {
                predicates.add(cb.equal(root.get("sender").get("id"), senderId));
            }
            if (redeemerId != null) {
                predicates.add(cb.equal(root.get("redeemer").get("id"), redeemerId));
            }
            if (issuedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("issuedAt"), issuedFrom));
            }
            if (issuedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("issuedAt"), issuedTo));
            }
            if (redeemedFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("redeemedAt"), redeemedFrom));
            }
            if (redeemedTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("redeemedAt"), redeemedTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
    private GiftVoucherAdminResponse toResponse(GiftVoucher voucher) {
        return new GiftVoucherAdminResponse(
                voucher.getId(),
                voucher.getVoucherCode(),
                voucher.getRecipientEmail(),
                voucher.getStatus(),
                voucher.getIssuedAt(),
                voucher.getRedeemedAt(),
                voucher.getExpirationDate(),
                voucher.getDescription(),
                new GiftVoucherAdminResponse.CourseInfo(
                        voucher.getCourse().getId(),
                        voucher.getCourse().getTitle(),
                        voucher.getCourse().getSlug()
                ),
                new GiftVoucherAdminResponse.UserInfo(
                        voucher.getSender().getId(),
                        voucher.getSender().getEmail(),
                        voucher.getSender().getUsername()
                ),
                voucher.getRedeemer() != null 
                        ? new GiftVoucherAdminResponse.UserInfo(
                                voucher.getRedeemer().getId(),
                                voucher.getRedeemer().getEmail(),
                                voucher.getRedeemer().getUsername()
                        )
                        : null
        );
    }
}
