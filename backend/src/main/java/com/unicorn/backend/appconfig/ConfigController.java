package com.unicorn.backend.appconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for application configuration management.
 */
@RestController
@RequiredArgsConstructor
public class ConfigController {

    private final AppConfigService configService;

    /**
     * Get public configuration for mobile app.
     * This endpoint is public and used by the mobile app on splash screen.
     * 
     * GET /api/v1/public/config
     */
    @GetMapping("/api/v1/public/config")
    public ResponseEntity<Map<String, Object>> getPublicConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("version", configService.getVersion());
        response.put("data", configService.getAllAsMap());
        return ResponseEntity.ok(response);
    }

    /**
     * Get all configs for admin dashboard.
     * 
     * GET /api/v1/admin/config
     */
    @GetMapping("/api/v1/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AppConfig>> getAllConfigs() {
        return ResponseEntity.ok(configService.getAll());
    }

    /**
     * Get configs grouped by category.
     * 
     * GET /api/v1/admin/config/grouped
     */
    @GetMapping("/api/v1/admin/config/grouped")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, List<AppConfig>>> getConfigsGrouped() {
        return ResponseEntity.ok(configService.getAllGroupedByCategory());
    }

    /**
     * Update a single config value.
     * 
     * PUT /api/v1/admin/config/{key}
     */
    @PutMapping("/api/v1/admin/config/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppConfig> updateConfig(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null) {
            return ResponseEntity.badRequest().build();
        }
        AppConfig updated = configService.updateValue(key, value);
        return ResponseEntity.ok(updated);
    }

    /**
     * Batch update multiple configs.
     * 
     * PUT /api/v1/admin/config
     */
    @PutMapping("/api/v1/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> batchUpdateConfigs(@RequestBody Map<String, String> updates) {
        for (Map.Entry<String, String> entry : updates.entrySet()) {
            configService.updateValue(entry.getKey(), entry.getValue());
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Configuration updated successfully");
        response.put("version", configService.getVersion());
        response.put("updatedCount", updates.size());
        return ResponseEntity.ok(response);
    }

    /**
     * Create or update a config.
     * 
     * POST /api/v1/admin/config
     */
    @PostMapping("/api/v1/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppConfig> createOrUpdateConfig(@RequestBody ConfigCreateRequest request) {
        AppConfig config = configService.upsert(
                request.getKey(),
                request.getValue(),
                request.getDescription(),
                request.getCategory(),
                request.getValueType());
        return ResponseEntity.ok(config);
    }

    /**
     * Get current config version.
     * 
     * GET /api/v1/admin/config/version
     */
    @GetMapping("/api/v1/admin/config/version")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Integer>> getConfigVersion() {
        return ResponseEntity.ok(Map.of("version", configService.getVersion()));
    }
}
