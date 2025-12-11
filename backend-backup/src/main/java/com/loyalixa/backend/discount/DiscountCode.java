package com.loyalixa.backend.discount;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.loyalixa.backend.user.User;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "discount_codes")
public class DiscountCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(nullable = false, length = 20)
    private String discountType;
    @Column(nullable = false)
    private BigDecimal discountValue;
    private Integer maxUses;
    private Integer currentUses = 0;
    @Column(nullable = false)
    private Boolean isPrivate = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = true)
    private User updatedBy;
    private LocalDateTime validUntil;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @Column(nullable = false, length = 20)
    private String applicableTo = "COURSES";
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "discount_courses", joinColumns = @JoinColumn(name = "discount_id"), inverseJoinColumns = @JoinColumn(name = "course_id"))
    private Set<com.loyalixa.backend.course.Course> applicableCourses;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "discount_products", joinColumns = @JoinColumn(name = "discount_id"), inverseJoinColumns = @JoinColumn(name = "product_id"))
    private Set<com.loyalixa.backend.shop.Product> applicableProducts;
}