package com.loyalixa.backend.discount;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_discounts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "discount_code_id"})
})
public class UserDiscount implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_code_id", nullable = false)
    private DiscountCode discountCode;
    @Column(nullable = false)
    private Boolean isUsed = false; 
}