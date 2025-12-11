package com.loyalixa.backend.shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, UUID> {
    List<ProductReview> findByProductIdAndStatus(UUID productId, String status);
    Page<ProductReview> findByProductIdAndStatus(UUID productId, String status, Pageable pageable);
    List<ProductReview> findByProductIdAndIsFeaturedTrueAndStatus(UUID productId, String status);
    Optional<ProductReview> findByProductIdAndUserId(UUID productId, UUID userId);
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Double getAverageRating(@Param("productId") UUID productId);
    @Query("SELECT COUNT(r) FROM ProductReview r WHERE r.product.id = :productId AND r.status = 'APPROVED'")
    Long getReviewCount(@Param("productId") UUID productId);
}
