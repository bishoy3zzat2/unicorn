package com.unicorn.backend.appconfig;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing application configuration.
 * Used for dynamic settings that can be updated via Admin Dashboard.
 * Mobile app syncs these values on startup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_config")
public class AppConfig {

    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String value;

    @Column(length = 500)
    private String description;

    @Column(length = 50)
    private String category;

    @Column(name = "value_type", length = 20)
    @Builder.Default
    private String valueType = "STRING"; // STRING, NUMBER, BOOLEAN, JSON

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
