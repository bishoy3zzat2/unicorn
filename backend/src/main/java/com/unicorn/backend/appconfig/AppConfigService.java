package com.unicorn.backend.appconfig;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing application configuration.
 */
@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository configRepository;

    /**
     * Get a config value by key.
     */
    public Optional<String> getValue(String key) {
        return configRepository.findById(key).map(AppConfig::getValue);
    }

    /**
     * Get a config value with default fallback.
     */
    public String getValue(String key, String defaultValue) {
        return getValue(key).orElse(defaultValue);
    }

    /**
     * Get a numeric config value.
     */
    public int getIntValue(String key, int defaultValue) {
        return getValue(key).map(Integer::parseInt).orElse(defaultValue);
    }

    /**
     * Get all configs as a map.
     */
    public Map<String, String> getAllAsMap() {
        Map<String, String> configMap = new HashMap<>();
        configRepository.findAll().forEach(config -> configMap.put(config.getKey(), config.getValue()));
        return configMap;
    }

    /**
     * Get all configs grouped by category.
     */
    public Map<String, List<AppConfig>> getAllGroupedByCategory() {
        Map<String, List<AppConfig>> grouped = new HashMap<>();
        List<AppConfig> allConfigs = configRepository.findAllByOrderByCategoryAscKeyAsc();

        for (AppConfig config : allConfigs) {
            String category = config.getCategory() != null ? config.getCategory() : "general";
            grouped.computeIfAbsent(category, k -> new java.util.ArrayList<>()).add(config);
        }
        return grouped;
    }

    /**
     * Get all configs.
     */
    public List<AppConfig> getAll() {
        return configRepository.findAllByOrderByCategoryAscKeyAsc();
    }

    /**
     * Update a config value.
     */
    @Transactional
    public AppConfig updateValue(String key, String value) {
        AppConfig config = configRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));
        config.setValue(value);

        // Auto-increment version when any config is updated
        incrementVersion();

        return configRepository.save(config);
    }

    /**
     * Create or update a config.
     */
    @Transactional
    public AppConfig upsert(String key, String value, String description, String category, String valueType) {
        AppConfig config = configRepository.findById(key)
                .orElse(AppConfig.builder().key(key).build());

        config.setValue(value);
        if (description != null)
            config.setDescription(description);
        if (category != null)
            config.setCategory(category);
        if (valueType != null)
            config.setValueType(valueType);

        return configRepository.save(config);
    }

    /**
     * Get current config version for mobile sync.
     */
    public int getVersion() {
        return getIntValue("config_version", 1);
    }

    /**
     * Increment the config version.
     */
    @Transactional
    public void incrementVersion() {
        int currentVersion = getVersion();
        AppConfig versionConfig = configRepository.findById("config_version")
                .orElse(AppConfig.builder()
                        .key("config_version")
                        .category("system")
                        .valueType("NUMBER")
                        .description("Configuration version for mobile app sync")
                        .build());
        versionConfig.setValue(String.valueOf(currentVersion + 1));
        configRepository.save(versionConfig);
    }

    /**
     * Initialize default configs if not exist.
     */
    @Transactional
    public void initializeDefaults() {
        // Pricing
        upsertIfNotExists("pricing_pro", "299", "Pro plan monthly price", "pricing", "NUMBER");
        upsertIfNotExists("pricing_elite", "799", "Elite plan monthly price", "pricing", "NUMBER");

        // Limits
        upsertIfNotExists("max_post_length", "2000", "Maximum post content length", "limits", "NUMBER");
        upsertIfNotExists("max_comment_length", "1000", "Maximum comment length", "limits", "NUMBER");
        upsertIfNotExists("nudge_limit_free", "4", "Weekly nudge limit for free users", "limits", "NUMBER");
        upsertIfNotExists("nudge_limit_pro", "12", "Flexible nudge limit for pro users", "limits", "NUMBER");

        // System
        upsertIfNotExists("config_version", "1", "Configuration version for mobile app sync", "system", "NUMBER");
    }

    private void upsertIfNotExists(String key, String value, String description, String category, String valueType) {
        if (!configRepository.existsById(key)) {
            upsert(key, value, description, category, valueType);
        }
    }
}
