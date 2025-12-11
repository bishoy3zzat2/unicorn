package com.loyalixa.backend.user.preferences;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_notification_settings",
       indexes = {
           @Index(name = "idx_uns_user", columnList = "user_id"),
           @Index(name = "idx_uns_user_type", columnList = "user_id,preference_type", unique = true)
       })
public class UserNotificationSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Column(name = "preference_type", nullable = false, length = 100)
    private String preferenceType;  
    @Column(name = "is_enabled", nullable = false)
    private boolean isEnabled = true;
}
