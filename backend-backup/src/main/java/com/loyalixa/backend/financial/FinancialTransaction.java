package com.loyalixa.backend.financial;
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
@EqualsAndHashCode(exclude = {"account", "processedBy"})
@Entity
@Table(name = "financial_transactions")
public class FinancialTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinancialAccount account;
    @Column(nullable = false, length = 30)
    private String transactionType; 
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "balance_before", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceBefore;
    @Column(name = "balance_after", nullable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;
    @Column(nullable = false, length = 3)
    private String currency = "EGP";
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(name = "reference_id")
    private UUID referenceId;
    @Column(name = "reference_type", length = 50)
    private String referenceType;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by")
    private User processedBy;
    @Column(nullable = false, length = 20)
    private String status = "COMPLETED";  
    @Column(name = "discount_reason", columnDefinition = "TEXT")
    private String discountReason;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
