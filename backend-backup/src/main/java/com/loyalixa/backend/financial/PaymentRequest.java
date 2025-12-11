package com.loyalixa.backend.financial;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "payment_requests")
public class PaymentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinancialAccount account;
    @Column(nullable = false, length = 20)
    private String requestType;  
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency = "EGP";
    @Column(columnDefinition = "TEXT")
    private String reason;
    @Column(nullable = false, length = 20)
    private String status = "PENDING";  
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "payment_method", length = 20)
    private String paymentMethod;  
    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
