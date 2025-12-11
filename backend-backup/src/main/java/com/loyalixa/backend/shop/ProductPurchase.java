package com.loyalixa.backend.shop;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "shop_product_purchases")
public class ProductPurchase {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;  
    @Column(precision = 19, scale = 2)
    private BigDecimal amountPaid;
    @Column(name = "coins_paid", precision = 19, scale = 2)
    private BigDecimal coinsPaid;
    @Column(length = 10)
    private String currency;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";  
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime purchasedAt;
}
