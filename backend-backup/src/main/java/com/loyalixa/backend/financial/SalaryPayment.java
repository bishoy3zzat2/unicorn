package com.loyalixa.backend.financial;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "salary_payments")
public class SalaryPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinancialAccount account;
    @Column(name = "payment_month", nullable = false)
    private LocalDate paymentMonth;  
    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;
    @Column(name = "bonus", precision = 19, scale = 2)
    private BigDecimal bonus = BigDecimal.ZERO;
    @Column(name = "deductions", precision = 19, scale = 2)
    private BigDecimal deductions = BigDecimal.ZERO;
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;
    @Column(nullable = false, length = 3)
    private String currency = "EGP";
    @Column(nullable = false, length = 20)
    private String status = "PENDING";  
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "deduction_details", columnDefinition = "TEXT")
    private String deductionDetails;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
