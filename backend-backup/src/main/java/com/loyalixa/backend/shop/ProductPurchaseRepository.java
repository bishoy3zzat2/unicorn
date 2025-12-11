package com.loyalixa.backend.shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductPurchaseRepository extends JpaRepository<ProductPurchase, UUID> {

    List<ProductPurchase> findByUserId(UUID userId);

    Page<ProductPurchase> findByUserId(UUID userId, Pageable pageable);

    List<ProductPurchase> findByProductId(UUID productId);

    Optional<ProductPurchase> findByProductIdAndUserId(UUID productId, UUID userId);

    @Query("SELECT COUNT(p) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = 'COMPLETED'")
    Long getPurchaseCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = 'COMPLETED'")
    Long getUniqueBuyerCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(p) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'MONEY' AND p.status = 'COMPLETED'")
    Long getPurchasesByMoneyCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(p) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'COINS' AND p.status = 'COMPLETED'")
    Long getPurchasesByCoinsCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'MONEY' AND p.status = 'COMPLETED'")
    Long getUniqueBuyersByMoneyCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(DISTINCT p.user.id) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'COINS' AND p.status = 'COMPLETED'")
    Long getUniqueBuyersByCoinsCount(@Param("productId") UUID productId);

    @Query("SELECT COUNT(p) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = :status")
    Long getPurchasesByStatusCount(@Param("productId") UUID productId, @Param("status") String status);

    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'MONEY' AND p.status = 'COMPLETED'")
    BigDecimal getTotalRevenueMoney(@Param("productId") UUID productId);

    @Query("SELECT COALESCE(SUM(p.coinsPaid), 0) FROM ProductPurchase p WHERE p.product.id = :productId AND p.paymentMethod = 'COINS' AND p.status = 'COMPLETED'")
    BigDecimal getTotalRevenueCoins(@Param("productId") UUID productId);

    @Query("SELECT MIN(p.purchasedAt) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = 'COMPLETED'")
    LocalDateTime getFirstPurchaseDate(@Param("productId") UUID productId);

    @Query("SELECT MAX(p.purchasedAt) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = 'COMPLETED'")
    LocalDateTime getLastPurchaseDate(@Param("productId") UUID productId);

    @Query("SELECT COUNT(p) FROM ProductPurchase p WHERE p.product.id = :productId AND p.status = 'COMPLETED' AND p.purchasedAt >= :since")
    Long getPurchasesSinceDate(@Param("productId") UUID productId, @Param("since") LocalDateTime since);
}
