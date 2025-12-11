package com.loyalixa.backend.course;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "course_providers")
public class CourseProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false, unique = true, length = 200)
    private String name;  
    @Column(nullable = false, unique = true, length = 250)
    private String slug;  
    @Column(length = 500)
    private String logoUrl;  
    @Column(length = 500)
    private String websiteUrl;  
    @Column(columnDefinition = "TEXT")
    private String description;  
    @Column(nullable = false)
    private Boolean isActive = true;  
    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<ProviderSocialLink> socialLinks = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = true)
    private User createdBy;  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id", nullable = true)
    private User updatedBy;  
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
