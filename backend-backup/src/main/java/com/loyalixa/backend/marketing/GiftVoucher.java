package com.loyalixa.backend.marketing;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.course.Course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "gift_vouchers")
public class GiftVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String voucherCode;  
    @Column(nullable = false, length = 255)
    private String recipientEmail;  
    @Column(columnDefinition = "TEXT", nullable = true)
    private String description;  
    @Column(nullable = true)
    private LocalDateTime expirationDate;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redeemer_id", nullable = true)
    private User redeemer;  
    @Column(nullable = false)
    private String status = "ISSUED";  
    private LocalDateTime redeemedAt;
    @CreationTimestamp
    private LocalDateTime issuedAt;
}