package com.loyalixa.backend.marketing;
import com.loyalixa.backend.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@Entity
@Table(name = "newsletter_subscribers")
public class NewsletterSubscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 255)
    private String email; 
    @Column(nullable = false)
    private Boolean isActive = true;  
    @CreationTimestamp
    private LocalDateTime subscribedAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(length = 1024)
    private String userAgentRaw;
    @Column(length = 100)
    private String browser;
    @Column(length = 100)
    private String operatingSystem;
    @Column(length = 50)
    private String deviceType;  
    @Column(length = 100)
    private String ipAddress;
    @Column(length = 255)
    private String acceptLanguage;
    @Column(length = 512)
    private String referrer;
    @Column(length = 255)
    private String host;  
    @Column(length = 255)
    private String origin;  
    @Column(length = 100)
    private String acceptEncoding;  
    @Column(length = 10)
    private String dnt;  
    private Integer screenWidth;
    private Integer screenHeight;
    private Integer viewportWidth;
    private Integer viewportHeight;
    private Double devicePixelRatio;
    @Column(length = 100)
    private String timezone;
    @Column(length = 100)
    private String platform;
    private Integer hardwareConcurrency;  
    private Double deviceMemoryGb;  
    private Boolean touchSupport;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    @JsonIgnore
    private User createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = true)
    @JsonIgnore
    private User updatedBy;
}