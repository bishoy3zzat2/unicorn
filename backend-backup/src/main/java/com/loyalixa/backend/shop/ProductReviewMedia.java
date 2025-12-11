package com.loyalixa.backend.shop;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "shop_product_review_media")
public class ProductReviewMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private ProductReview review;
    @Column(name = "media_type", nullable = false, length = 20)
    private String mediaType;  
    @Column(name = "media_url", nullable = false, length = 512)
    private String mediaUrl;
    @Column(name = "thumbnail_url", length = 512)
    private String thumbnailUrl;
    @Column(name = "alt_text", length = 255)
    private String altText;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
