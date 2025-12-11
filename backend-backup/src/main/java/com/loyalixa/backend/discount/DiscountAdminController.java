package com.loyalixa.backend.discount;
import com.loyalixa.backend.discount.dto.DiscountRequest;
import com.loyalixa.backend.discount.dto.DiscountResponse;
import com.loyalixa.backend.discount.dto.DiscountSearchRequest;
import com.loyalixa.backend.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;  
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/discounts")
public class DiscountAdminController {
    private final DiscountService discountService;
    public DiscountAdminController(DiscountService discountService) {
        this.discountService = discountService;
    }
    @GetMapping
    @PreAuthorize("hasAuthority('discount:get_all')")
    public ResponseEntity<List<DiscountResponse>> getAllCodes(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String discountType,
            @RequestParam(required = false) Boolean isPrivate,
            @RequestParam(required = false) Boolean isExpired,
            @RequestParam(required = false) Boolean isExhausted,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime validUntilFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime validUntilTo
    ) {
        boolean hasSearchParams = code != null || discountType != null || isPrivate != null ||
                isExpired != null || isExhausted != null || createdAtFrom != null ||
                createdAtTo != null || validUntilFrom != null || validUntilTo != null;
        if (hasSearchParams) {
            DiscountSearchRequest searchRequest = new DiscountSearchRequest(
                    code, discountType, isPrivate, isExpired, isExhausted,
                    createdAtFrom, createdAtTo, validUntilFrom, validUntilTo
            );
            return ResponseEntity.ok(discountService.searchDiscountCodes(searchRequest));
        } else {
            return ResponseEntity.ok(discountService.getAllDiscountCodes());
        }
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('discount:get_all')")
    public ResponseEntity<DiscountResponse> getCode(@PathVariable UUID id) {
        try {
            DiscountCode code = discountService.getDiscountCodeByIdWithRelations(id);
            DiscountResponse response = discountService.mapToDiscountResponse(code);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('discount:get_all')")
    public ResponseEntity<com.loyalixa.backend.discount.dto.DiscountDetailsResponse> getDiscountDetails(@PathVariable UUID id) {
        try {
            com.loyalixa.backend.discount.dto.DiscountDetailsResponse details = discountService.getDiscountCodeDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/users")
    @PreAuthorize("hasAuthority('discount:get_all')")
    public ResponseEntity<List<com.loyalixa.backend.user.dto.UserAdminResponse>> getEligibleUsers(@PathVariable UUID id) {
        try {
            List<com.loyalixa.backend.user.dto.UserAdminResponse> users = discountService.getEligibleUsers(id);
            return ResponseEntity.ok(users);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('discount:create')")
    public ResponseEntity<DiscountResponse> createCode(@Valid @RequestBody DiscountRequest request, @AuthenticationPrincipal User adminUser) {
        try {
            DiscountCode newCode = discountService.createDiscountCode(request, adminUser); 
            DiscountCode loadedCode = discountService.getDiscountCodeByIdWithRelations(newCode.getId());
            DiscountResponse response = discountService.mapToDiscountResponse(loadedCode);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('discount:update')")
    public ResponseEntity<DiscountResponse> updateCode(
            @PathVariable UUID id,
            @Valid @RequestBody DiscountRequest request,
            @AuthenticationPrincipal User adminUser  
    ) {
        try {
            if (adminUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            DiscountCode updatedCode = discountService.updateDiscountCode(id, request, adminUser);
            DiscountCode loadedCode = discountService.getDiscountCodeByIdWithRelations(updatedCode.getId());
            DiscountResponse response = discountService.mapToDiscountResponse(loadedCode);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); 
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && e.getMessage().contains("expired")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('discount:delete')")
    public ResponseEntity<?> deleteCode(@PathVariable UUID id) {
        try {
            discountService.deleteDiscountCode(id);
            return ResponseEntity.noContent().build(); 
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage())); 
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage())); 
        }
    }
}