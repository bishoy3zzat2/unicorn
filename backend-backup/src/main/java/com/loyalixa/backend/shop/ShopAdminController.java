package com.loyalixa.backend.shop;
import com.loyalixa.backend.shop.dto.*;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/shop")
public class ShopAdminController {
    private final ShopService shopService;
    public ShopAdminController(ShopService shopService) {
        this.shopService = shopService;
    }
    @GetMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:view')")
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        Page<ProductResponse> products = shopService.getAllProducts(page, size, status, search);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:view')")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        try {
            ProductResponse product = shopService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:create')")
    public ResponseEntity<?> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ProductResponse product = shopService.createProduct(request, currentUser);
            return new ResponseEntity<>(product, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:update')")
    public ResponseEntity<?> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ProductResponse product = shopService.updateProduct(id, request, currentUser);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:delete')")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID id) {
        try {
            shopService.deleteProduct(id);
            return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/products/{productId}/reviews")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:view')")
    public ResponseEntity<Page<ProductReviewResponse>> getProductReviews(
            @PathVariable UUID productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status
    ) {
        Page<ProductReviewResponse> reviews = shopService.getProductReviews(productId, page, size, status);
        return ResponseEntity.ok(reviews);
    }
    @PutMapping("/products/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:approve')")
    public ResponseEntity<?> approveProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ProductResponse product = shopService.approveProduct(id, currentUser);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/products/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:reject')")
    public ResponseEntity<?> rejectProduct(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            ProductResponse product = shopService.rejectProduct(id, currentUser);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/reviews/{reviewId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:update')")
    public ResponseEntity<?> updateReviewStatus(
            @PathVariable UUID reviewId,
            @RequestBody Map<String, String> request
    ) {
        try {
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }
            ProductReviewResponse review = shopService.updateReviewStatus(reviewId, status);
            return ResponseEntity.ok(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/products/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:approve')")
    public ResponseEntity<?> updateProductStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            String status = request.get("status");
            if (status == null || status.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status is required"));
            }
            ProductResponse product = shopService.updateProductStatus(id, status, currentUser);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping("/products/{id}/purchase-stats")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('shop:view')")
    public ResponseEntity<?> getProductPurchaseStats(@PathVariable UUID id) {
        try {
            ProductPurchaseStatsResponse stats = shopService.getProductPurchaseStats(id);
            return ResponseEntity.ok(stats);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
