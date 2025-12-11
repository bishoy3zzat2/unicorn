package com.loyalixa.backend.course;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "provider_social_links")
public class ProviderSocialLink {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private CourseProvider provider;
    @Column(nullable = false, length = 50)
    private String platform;  
    @Column(length = 200)
    private String iconClass;  
    @Column(length = 500)
    private String url;  
    @Column(length = 200)
    private String username;  
    @Column(nullable = false)
    private Boolean isUsernameBased = false;  
    @Column(length = 100)
    private String displayText;  
    @Column(nullable = false)
    private Integer orderIndex = 0;  
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
