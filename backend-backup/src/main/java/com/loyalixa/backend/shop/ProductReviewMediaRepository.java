package com.loyalixa.backend.shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface ProductReviewMediaRepository extends JpaRepository<ProductReviewMedia, UUID> {
    List<ProductReviewMedia> findByReviewId(UUID reviewId);
    void deleteByReviewId(UUID reviewId);
}
