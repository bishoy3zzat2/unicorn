package com.loyalixa.backend.financial;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"user", "transactions", "paymentRequests", "taskPayments", "salaryPayments"})
@Entity
@Table(name = "financial_accounts")
public class FinancialAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;
    @Column(nullable = false, length = 20)
    private String paymentMethod;  
    @Column(name = "employment_type", length = 20)
    private String employmentType;  
    @Column(name = "has_fixed_salary")
    private Boolean hasFixedSalary;  
    @Column(name = "employment_start_date")
    private LocalDate employmentStartDate;
    @Column(name = "employment_end_date")
    private LocalDate employmentEndDate;
    @Column(name = "work_schedule", columnDefinition = "TEXT")
    private String workSchedule;
    @Column(name = "hours_per_week")
    private Integer hoursPerWeek;
    @Column(name = "work_instructions", columnDefinition = "TEXT")
    private String workInstructions;
    @Column(name = "monthly_salary", precision = 19, scale = 2)
    private BigDecimal monthlySalary;
    @Column(name = "salary_currency", length = 3)
    private String salaryCurrency;
    @Column(name = "salary_payment_day")
    private Integer salaryPaymentDay;
    @Column(name = "monthly_bonus", precision = 19, scale = 2)
    private BigDecimal monthlyBonus;
    @Column(nullable = false, length = 3)
    private String currency = "EGP";
    @Column(name = "bank_name", length = 100)
    private String bankName;
    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;
    @Column(name = "bank_iban", length = 50)
    private String bankIban;
    @Column(name = "bank_swift_code", length = 20)
    private String bankSwiftCode;
    @Column(name = "wallet_type", length = 20)
    private String walletType;  
    @Column(name = "wallet_number", length = 20)
    private String walletNumber;
    @Column(name = "card_type", length = 30)
    private String cardType;  
    @Column(name = "card_number", length = 50)
    private String cardNumber;
    @Column(name = "card_holder_name", length = 100)
    private String cardHolderName;
    @Column(name = "card_country", length = 100)
    private String cardCountry;  
    @Column(name = "card_bank_name", length = 100)
    private String cardBankName;
    @Column(name = "card_expiry_date", length = 10)
    private String cardExpiryDate;  
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";  
    @Column(columnDefinition = "TEXT")
    private String notes;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<FinancialTransaction> transactions;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentRequest> paymentRequests;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TaskPayment> taskPayments;
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<SalaryPayment> salaryPayments;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
