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
@Table(name = "task_payments")
public class TaskPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private FinancialAccount account;
    @Column(nullable = false, length = 255)
    private String taskTitle;
    @Column(columnDefinition = "TEXT")
    private String taskDescription;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 3)
    private String currency = "EGP";
    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;
    @Column(name = "payment_month", nullable = false)
    private LocalDate paymentMonth;  
    @Column(nullable = false, length = 20)
    private String status = "PENDING";  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "external_task_id", length = 100)
    private String externalTaskId;
    @Column(name = "completion_status", length = 20)
    private String completionStatus = "PENDING";  
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;
    @Column(name = "attachments", columnDefinition = "TEXT")
    private String attachments;  
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
