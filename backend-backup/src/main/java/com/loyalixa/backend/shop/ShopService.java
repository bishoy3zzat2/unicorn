package com.loyalixa.backend.shop;

import com.loyalixa.backend.shop.dto.*;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final ProductRepository productRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductReviewRepository productReviewRepository;
    private final ProductReviewMediaRepository productReviewMediaRepository;
    private final ProductPurchaseRepository productPurchaseRepository;
    private final UserRepository userRepository;

    public ShopService(ProductRepository productRepository, ProductMediaRepository productMediaRepository,
            ProductReviewRepository productReviewRepository, ProductReviewMediaRepository productReviewMediaRepository,
            ProductPurchaseRepository productPurchaseRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.productMediaRepository = productMediaRepository;
        this.productReviewRepository = productReviewRepository;
        this.productReviewMediaRepository = productReviewMediaRepository;
        this.productPurchaseRepository = productPurchaseRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String status, String search) {
        Pageable pageable = PageRequest.of(page, size);

        if (search != null && !search.trim().isEmpty()) {
            String searchStatus = status != null ? status : "PUBLISHED";
            return productRepository.searchProducts(searchStatus, search, pageable)
                    .map(this::mapToProductResponse);
        }

        if (status != null) {
            return productRepository.findByStatus(status, pageable)
                    .map(this::mapToProductResponse);
        }

        return productRepository.findAll(pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        return mapToProductResponse(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request, User createdBy) {
        if (productRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Product with slug '" + request.slug() + "' already exists.");
        }

        Product product = new Product();
        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setShortDescription(request.shortDescription());
        product.setPrice(request.price());
        product.setPriceInCoins(request.priceInCoins());
        product.setCurrency(request.currency() != null ? request.currency() : "EGP");
        product.setPaymentMethod(request.paymentMethod());
        product.setStatus("PENDING"); // Always set to PENDING on create
        product.setStockQuantity(request.stockQuantity());
        product.setIsFeatured(request.isFeatured() != null ? request.isFeatured() : false);
        product.setCategory(request.category());
        product.setCreatedBy(createdBy);
        product.setUpdatedBy(null); // No updates yet

        Product saved = productRepository.save(product);

        // Save media
        if (request.media() != null && !request.media().isEmpty()) {
            int orderIndex = 0;
            for (ProductRequest.MediaRequest mediaReq : request.media()) {
                ProductMedia media = new ProductMedia();
                media.setProduct(saved);
                media.setMediaType(mediaReq.mediaType());
                media.setMediaUrl(mediaReq.mediaUrl());
                media.setThumbnailUrl(mediaReq.thumbnailUrl());
                media.setOrderIndex(mediaReq.orderIndex() != null ? mediaReq.orderIndex() : orderIndex++);
                media.setAltText(mediaReq.altText());

                // Video properties
                if ("VIDEO".equals(mediaReq.mediaType())) {
                    media.setAutoplay(mediaReq.autoplay() != null ? mediaReq.autoplay() : false);
                    media.setMuted(mediaReq.muted() != null ? mediaReq.muted() : false);
                    media.setLoop(mediaReq.loop() != null ? mediaReq.loop() : false);
                    media.setControls(mediaReq.controls() != null ? mediaReq.controls() : true);
                }

                productMediaRepository.save(media);
            }
        }

        return mapToProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request, User updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Check slug uniqueness if changed
        if (!product.getSlug().equals(request.slug()) && productRepository.existsBySlug(request.slug())) {
            throw new IllegalStateException("Product with slug '" + request.slug() + "' already exists.");
        }

        product.setName(request.name());
        product.setSlug(request.slug());
        product.setDescription(request.description());
        product.setShortDescription(request.shortDescription());
        product.setPrice(request.price());
        product.setPriceInCoins(request.priceInCoins());
        product.setCurrency(request.currency());
        product.setPaymentMethod(request.paymentMethod());
        // Don't allow status change through update - only through approve/publish
        // endpoints
        // If product was PUBLISHED and is being updated, set back to PENDING for review
        if ("PUBLISHED".equals(product.getStatus())) {
            product.setStatus("PENDING");
        }
        product.setStockQuantity(request.stockQuantity());
        product.setIsFeatured(request.isFeatured());
        product.setCategory(request.category());
        product.setUpdatedBy(updatedBy);

        // Update media
        if (request.media() != null) {
            // Clear existing media using the Set (cascade will handle deletion)
            product.getMedia().clear();
            productRepository.flush(); // Force flush to ensure deletion happens before adding new media

            // Add new media
            int orderIndex = 0;
            for (ProductRequest.MediaRequest mediaReq : request.media()) {
                ProductMedia media = new ProductMedia();
                media.setProduct(product);
                media.setMediaType(mediaReq.mediaType());
                media.setMediaUrl(mediaReq.mediaUrl());
                media.setThumbnailUrl(mediaReq.thumbnailUrl());
                media.setOrderIndex(mediaReq.orderIndex() != null ? mediaReq.orderIndex() : orderIndex++);
                media.setAltText(mediaReq.altText());

                // Video properties
                if ("VIDEO".equals(mediaReq.mediaType())) {
                    media.setAutoplay(mediaReq.autoplay() != null ? mediaReq.autoplay() : false);
                    media.setMuted(mediaReq.muted() != null ? mediaReq.muted() : false);
                    media.setLoop(mediaReq.loop() != null ? mediaReq.loop() : false);
                    media.setControls(mediaReq.controls() != null ? mediaReq.controls() : true);
                }

                product.getMedia().add(media);
            }
        } else {
            // If media is null, clear all existing media
            product.getMedia().clear();
        }

        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    public ProductResponse approveProduct(UUID id, User approvedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!"PENDING".equals(product.getStatus())) {
            throw new IllegalStateException("Only PENDING products can be approved");
        }

        product.setStatus("PUBLISHED");
        product.setUpdatedBy(approvedBy);
        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    public ProductResponse rejectProduct(UUID id, User rejectedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!"PENDING".equals(product.getStatus())) {
            throw new IllegalStateException("Only PENDING products can be rejected");
        }

        product.setStatus("REJECTED");
        product.setUpdatedBy(rejectedBy);
        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    public ProductResponse updateProductStatus(UUID id, String status, User updatedBy) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Validate status
        List<String> validStatuses = List.of("PENDING", "PUBLISHED", "ARCHIVED", "OUT_OF_STOCK", "REJECTED");
        if (!validStatuses.contains(status)) {
            throw new IllegalStateException("Invalid status: " + status + ". Valid statuses are: " + validStatuses);
        }

        product.setStatus(status);
        product.setUpdatedBy(updatedBy);
        Product saved = productRepository.save(product);
        return mapToProductResponse(saved);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Product not found");
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ProductReviewResponse> getProductReviews(UUID productId, int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        String reviewStatus = status != null ? status : "APPROVED";
        return productReviewRepository.findByProductIdAndStatus(productId, reviewStatus, pageable)
                .map(this::mapToReviewResponse);
    }

    @Transactional
    public ProductReviewResponse createProductReview(UUID productId, ProductReviewRequest request, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Check if user already reviewed this product
        Optional<ProductReview> existingReview = productReviewRepository.findByProductIdAndUserId(productId,
                user.getId());
        if (existingReview.isPresent()) {
            throw new IllegalStateException("You have already reviewed this product.");
        }

        // Check if user purchased the product
        Optional<ProductPurchase> purchase = productPurchaseRepository.findByProductIdAndUserId(productId,
                user.getId());
        if (purchase.isEmpty() || !"COMPLETED".equals(purchase.get().getStatus())) {
            throw new IllegalStateException("You must purchase the product before reviewing it.");
        }

        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());
        review.setStatus("PENDING");
        review.setIsFeatured(false);

        ProductReview saved = productReviewRepository.save(review);

        // Save review media
        if (request.media() != null && !request.media().isEmpty()) {
            for (ProductReviewRequest.ReviewMediaRequest mediaReq : request.media()) {
                ProductReviewMedia media = new ProductReviewMedia();
                media.setReview(saved);
                media.setMediaType(mediaReq.mediaType());
                media.setMediaUrl(mediaReq.mediaUrl());
                media.setThumbnailUrl(mediaReq.thumbnailUrl());
                media.setAltText(mediaReq.altText());
                productReviewMediaRepository.save(media);
            }
        }

        return mapToReviewResponse(saved);
    }

    @Transactional
    public ProductReviewResponse updateReviewStatus(UUID reviewId, String status) {
        ProductReview review = productReviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setStatus(status);
        ProductReview saved = productReviewRepository.save(review);
        return mapToReviewResponse(saved);
    }

    // Mapper methods
    private ProductResponse mapToProductResponse(Product product) {
        List<ProductResponse.MediaResponse> media = product.getMedia() != null ? product.getMedia().stream()
                .map(m -> new ProductResponse.MediaResponse(
                        m.getId(), m.getMediaType(), m.getMediaUrl(),
                        m.getThumbnailUrl(), m.getOrderIndex(), m.getAltText(),
                        m.getAutoplay(), m.getMuted(), m.getLoop(), m.getControls()))
                .collect(Collectors.toList()) : new ArrayList<>();

        Double avgRating = productReviewRepository.getAverageRating(product.getId());
        Long reviewCount = productReviewRepository.getReviewCount(product.getId());
        Long purchaseCount = productPurchaseRepository.getPurchaseCount(product.getId());

        return new ProductResponse(
                product.getId(), product.getName(), product.getSlug(),
                product.getDescription(), product.getShortDescription(),
                product.getPrice(), product.getPriceInCoins(), product.getCurrency(),
                product.getPaymentMethod(), product.getStatus(), product.getStockQuantity(),
                product.getIsFeatured(), product.getCategory(),
                product.getCreatedBy() != null ? product.getCreatedBy().getId() : null,
                product.getCreatedBy() != null ? product.getCreatedBy().getEmail() : null,
                product.getUpdatedBy() != null ? product.getUpdatedBy().getId() : null,
                product.getUpdatedBy() != null ? product.getUpdatedBy().getEmail() : null,
                media, avgRating, reviewCount, purchaseCount,
                product.getCreatedAt(), product.getUpdatedAt());
    }

    private ProductReviewResponse mapToReviewResponse(ProductReview review) {
        List<ProductReviewResponse.ReviewMediaResponse> media = review.getMedia() != null ? review.getMedia().stream()
                .map(m -> new ProductReviewResponse.ReviewMediaResponse(
                        m.getId(), m.getMediaType(), m.getMediaUrl(),
                        m.getThumbnailUrl(), m.getAltText()))
                .collect(Collectors.toList()) : new ArrayList<>();

        return new ProductReviewResponse(
                review.getId(), review.getProduct().getId(),
                review.getUser().getId(), review.getUser().getUsername(),
                review.getUser().getEmail(),
                review.getUser().getUserProfile() != null ? review.getUser().getUserProfile().getAvatarUrl() : null,
                review.getRating(), review.getComment(), review.getStatus(),
                review.getIsFeatured(), media,
                review.getCreatedAt(), review.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public ProductPurchaseStatsResponse getProductPurchaseStats(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Total counts
        Long totalPurchases = (long) productPurchaseRepository.findByProductId(productId).size();
        Long totalUniqueBuyers = productPurchaseRepository.getUniqueBuyerCount(productId);
        Long completedPurchases = productPurchaseRepository.getPurchasesByStatusCount(productId, "COMPLETED");
        Long pendingPurchases = productPurchaseRepository.getPurchasesByStatusCount(productId, "PENDING");
        Long cancelledPurchases = productPurchaseRepository.getPurchasesByStatusCount(productId, "CANCELLED");
        Long refundedPurchases = productPurchaseRepository.getPurchasesByStatusCount(productId, "REFUNDED");

        // Payment method breakdown
        Long purchasesByMoney = productPurchaseRepository.getPurchasesByMoneyCount(productId);
        Long purchasesByCoins = productPurchaseRepository.getPurchasesByCoinsCount(productId);
        Long uniqueBuyersByMoney = productPurchaseRepository.getUniqueBuyersByMoneyCount(productId);
        Long uniqueBuyersByCoins = productPurchaseRepository.getUniqueBuyersByCoinsCount(productId);

        // Revenue statistics
        BigDecimal totalRevenueMoney = productPurchaseRepository.getTotalRevenueMoney(productId);
        BigDecimal totalRevenueCoins = productPurchaseRepository.getTotalRevenueCoins(productId);
        BigDecimal averagePurchaseValueMoney = purchasesByMoney > 0
                ? totalRevenueMoney.divide(BigDecimal.valueOf(purchasesByMoney), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal averagePurchaseValueCoins = purchasesByCoins > 0
                ? totalRevenueCoins.divide(BigDecimal.valueOf(purchasesByCoins), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Time-based statistics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime last30Days = now.minusDays(30);
        LocalDateTime last90Days = now.minusDays(90);

        Long purchasesLast7Days = productPurchaseRepository.getPurchasesSinceDate(productId, last7Days);
        Long purchasesLast30Days = productPurchaseRepository.getPurchasesSinceDate(productId, last30Days);
        Long purchasesLast90Days = productPurchaseRepository.getPurchasesSinceDate(productId, last90Days);

        LocalDateTime firstPurchaseDate = productPurchaseRepository.getFirstPurchaseDate(productId);
        LocalDateTime lastPurchaseDate = productPurchaseRepository.getLastPurchaseDate(productId);

        // Get recent purchases (last 10)
        List<ProductPurchase> recentPurchasesList = productPurchaseRepository.findByProductId(productId)
                .stream()
                .sorted((a, b) -> b.getPurchasedAt().compareTo(a.getPurchasedAt()))
                .limit(10)
                .collect(Collectors.toList());

        List<ProductPurchaseStatsResponse.RecentPurchaseInfo> recentPurchases = recentPurchasesList.stream()
                .map(p -> new ProductPurchaseStatsResponse.RecentPurchaseInfo(
                        p.getId(),
                        p.getUser().getId(),
                        p.getUser().getEmail(),
                        p.getPaymentMethod(),
                        p.getAmountPaid(),
                        p.getCoinsPaid(),
                        p.getStatus(),
                        p.getPurchasedAt()))
                .collect(Collectors.toList());

        return new ProductPurchaseStatsResponse(
                totalPurchases,
                totalUniqueBuyers != null ? totalUniqueBuyers : 0L,
                completedPurchases != null ? completedPurchases : 0L,
                pendingPurchases != null ? pendingPurchases : 0L,
                cancelledPurchases != null ? cancelledPurchases : 0L,
                refundedPurchases != null ? refundedPurchases : 0L,
                purchasesByMoney != null ? purchasesByMoney : 0L,
                purchasesByCoins != null ? purchasesByCoins : 0L,
                uniqueBuyersByMoney != null ? uniqueBuyersByMoney : 0L,
                uniqueBuyersByCoins != null ? uniqueBuyersByCoins : 0L,
                totalRevenueMoney != null ? totalRevenueMoney : BigDecimal.ZERO,
                totalRevenueCoins != null ? totalRevenueCoins : BigDecimal.ZERO,
                averagePurchaseValueMoney,
                averagePurchaseValueCoins,
                purchasesLast7Days != null ? purchasesLast7Days : 0L,
                purchasesLast30Days != null ? purchasesLast30Days : 0L,
                purchasesLast90Days != null ? purchasesLast90Days : 0L,
                firstPurchaseDate,
                lastPurchaseDate,
                totalRevenueCoins != null ? totalRevenueCoins : BigDecimal.ZERO,
                product.getCurrency(),
                recentPurchases);
    }
}
