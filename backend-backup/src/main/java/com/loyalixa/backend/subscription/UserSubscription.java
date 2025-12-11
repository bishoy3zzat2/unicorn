package com.loyalixa.backend.subscription;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";  
    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;
    @Column(name = "end_date")
    private LocalDateTime endDate;
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew = false;
    @Column(name = "last_renewed_at")
    private LocalDateTime lastRenewedAt;
    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount = 0;
    @Column(name = "payment_reference", length = 255)
    private String paymentReference;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    public boolean isActive() {
        if (!"ACTIVE".equals(status)) {
            return false;
        }
        if (endDate == null) {
            return true;  
        }
        return LocalDateTime.now().isBefore(endDate);
    }
    public boolean isExpired() {
        if (endDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(endDate);
    }
}
