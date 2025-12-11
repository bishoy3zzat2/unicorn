package com.loyalixa.backend.user.preferences;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
@Data
@NoArgsConstructor
@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;
    @Column(name = "ui_theme", nullable = false, length = 20)
    private String uiTheme = "LIGHT";
    @Column(name = "ui_language", nullable = false, length = 10)
    private String uiLanguage = "EN";
    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";
}
