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

    public List<AppConfig> getByCategory(String category) {
        return configRepository.findByCategory(category);
    }

    private volatile boolean cachedMaintenanceMode = false;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Initialize cache
        cachedMaintenanceMode = Boolean.parseBoolean(getValue("maintenance_mode", "false"));
    }

    public boolean isMaintenanceModeEnabled() {
        return cachedMaintenanceMode;
    }

    private void updateCachedMaintenanceMode(String key, String value) {
        if ("maintenance_mode".equals(key)) {
            cachedMaintenanceMode = Boolean.parseBoolean(value);
        }
    }

    /**
     * Update a config value.
     */
    @Transactional
    public AppConfig updateValue(String key, String value) {
        AppConfig config = configRepository.findById(key)
                .orElseThrow(() -> new RuntimeException("Config not found: " + key));
        config.setValue(value);

        updateCachedMaintenanceMode(key, value);

        // Auto-increment version when a config is updated,
        // UNLESS it's an exchange rate (dashboard only)
        if (!key.startsWith("rate_")) {
            incrementVersion();
        }

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

        updateCachedMaintenanceMode(key, value);

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
     * Note: Subscription pricing is NOT stored here - it's managed via Google Play
     * Console.
     * The actual prices are fetched from Google Play API when processing payments.
     */
    @Transactional
    public void initializeDefaults() {
        // Limits - General
        upsert("max_post_length", "2000", "Maximum post content length", "limits_general", "NUMBER");
        upsert("max_comment_length", "1000", "Maximum comment length", "limits_general", "NUMBER");
        upsert("max_bio_length", "250", "Maximum user bio length", "limits_general", "NUMBER");

        // Limits - Plans (Nudge)
        upsertIfNotExists("nudge.limit.free.monthly", "4", "Monthly nudge limit for FREE plan", "nudge", "NUMBER");
        upsertIfNotExists("nudge.limit.pro.monthly", "12", "Monthly nudge limit for PRO plan", "nudge", "NUMBER");
        upsertIfNotExists("nudge.cooldown.pro.days", "5", "Cooldown days between nudges to same investor (PRO)",
                "nudge", "NUMBER");
        upsertIfNotExists("nudge.cooldown.elite.days", "3", "Cooldown days between nudges to same investor (ELITE)",
                "nudge", "NUMBER");

        // System
        upsertIfNotExists("maintenance_mode", "false", "Enable maintenance mode", "system", "BOOLEAN");
        upsertIfNotExists("config_version", "1", "Configuration version for mobile app sync", "system", "NUMBER");

        // Exchange Rates (Base USD)
        upsertIfNotExists("rate_sar", "3.75", "Saudi Riyal Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_aed", "3.67", "UAE Dirham Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_egp", "50.50", "Egyptian Pound Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_qar", "3.64", "Qatari Riyal Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_kwd", "0.31", "Kuwaiti Dinar Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_bhd", "0.38", "Bahraini Dinar Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_omr", "0.38", "Omani Rial Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_jod", "0.71", "Jordanian Dinar Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_lbp", "89500", "Lebanese Pound Exchange Rate", "exchange_rates", "NUMBER");
        upsertIfNotExists("rate_mad", "10.0", "Moroccan Dirham Exchange Rate", "exchange_rates", "NUMBER");

        // Note: Android subscription product IDs are NOT stored here.
        // Mobile app gets product details directly from Google Play BillingClient.

        // Ensure cache is synced after defaults
        init();
    }

    private void upsertIfNotExists(String key, String value, String description, String category, String valueType) {
        if (!configRepository.existsById(key)) {
            upsert(key, value, description, category, valueType);
        }
    }
}
