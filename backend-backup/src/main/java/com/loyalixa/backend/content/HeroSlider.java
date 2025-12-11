package com.loyalixa.backend.content;
import com.loyalixa.backend.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@Entity
@Table(name = "hero_sliders")
public class HeroSlider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 500)
    private String mainTitle;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, length = 1000)
    private String mediaUrl;
    @Column(nullable = false, length = 20)
    private String mediaType;
    @Column(length = 100)
    private String buttonText;
    @Column(length = 1000)
    private String buttonLink;
    @Column(nullable = false)
    private Integer displayDurationMs = 1000;
    @Column(nullable = false)
    private Boolean autoplay = false;
    @Column(nullable = false)
    private Boolean loop = false;
    @Column(nullable = false)
    private Boolean muted = false;
    @Column(nullable = false)
    private Boolean controls = true;
    private Integer orderIndex;
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
