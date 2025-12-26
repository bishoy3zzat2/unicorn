package com.unicorn.backend.appconfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing application configuration.
 * Uses in-memory caching with TTL to reduce database reads.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final AppConfigRepository configRepository;

    // ==================== Config Cache ====================

    /**
     * In-memory cache for config values.
     * Key: config key, Value: cached value
     */
    private final Map<String, String> configCache = new ConcurrentHashMap<>();

    /**
     * Cache timestamp for TTL validation.
     */
    private volatile LocalDateTime cacheLoadedAt = null;

    /**
     * Cache TTL in minutes.
     */
    private static final int CACHE_TTL_MINUTES = 5;

    /**
     * Load all configs into cache.
     */
    private void loadCache() {
        Map<String, String> newCache = new HashMap<>();
        configRepository.findAll().forEach(config -> newCache.put(config.getKey(), config.getValue()));
        configCache.clear();
        configCache.putAll(newCache);
        cacheLoadedAt = LocalDateTime.now();
        log.debug("Config cache loaded with {} entries", configCache.size());
    }

    /**
     * Check if cache needs refresh.
     */
    private boolean isCacheStale() {
        if (cacheLoadedAt == null || configCache.isEmpty()) {
            return true;
        }
        return LocalDateTime.now().isAfter(cacheLoadedAt.plusMinutes(CACHE_TTL_MINUTES));
    }

    /**
     * Ensure cache is fresh.
     */
    private void ensureCacheFresh() {
        if (isCacheStale()) {
            synchronized (this) {
                if (isCacheStale()) {
                    loadCache();
                }
            }
        }
    }

    /**
     * Invalidate the cache (called after updates).
     */
    private void invalidateCache() {
        cacheLoadedAt = null;
    }

    // ==================== Public Methods ====================

    /**
     * Get a config value by key (CACHED).
     */
    public Optional<String> getValue(String key) {
        ensureCacheFresh();
        return Optional.ofNullable(configCache.get(key));
    }

    /**
     * Get a config value with default fallback (CACHED).
     */
    public String getValue(String key, String defaultValue) {
        return getValue(key).orElse(defaultValue);
    }

    /**
     * Get a numeric config value (CACHED).
     */
    public int getIntValue(String key, int defaultValue) {
        return getValue(key).map(v -> {
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }).orElse(defaultValue);
    }

    /**
     * Get a double config value (CACHED).
     */
    public double getDoubleValue(String key, double defaultValue) {
        return getValue(key).map(v -> {
            try {
                return Double.parseDouble(v);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }).orElse(defaultValue);
    }

    /**
     * Get all configs as a map (CACHED).
     */
    public Map<String, String> getAllAsMap() {
        ensureCacheFresh();
        return new HashMap<>(configCache);
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

        AppConfig saved = configRepository.save(config);
        invalidateCache();
        return saved;
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

        AppConfig saved = configRepository.save(config);
        invalidateCache();
        return saved;
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
        upsertIfNotExists("default_currency", "USD", "Default currency for payments", "system", "TEXT");

        // Verification
        upsertIfNotExists("investor_verification_fee", "99.00", "Investor verification fee (for record-keeping)",
                "verification", "NUMBER");

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

        // Feed Algorithm Configuration
        upsertIfNotExists("feed.decay.gravity", "1.5", "Time decay exponent (higher = faster decay)", "feed", "NUMBER");
        upsertIfNotExists("feed.like.points", "1", "Points per like", "feed", "NUMBER");
        upsertIfNotExists("feed.comment.points", "3", "Points per comment", "feed", "NUMBER");
        upsertIfNotExists("feed.share.points", "5", "Points per share", "feed", "NUMBER");
        upsertIfNotExists("feed.edit.penalty", "0.1", "Score reduction per edit", "feed", "NUMBER");
        upsertIfNotExists("feed.boost.free", "1.0", "FREE plan multiplier", "feed", "NUMBER");
        upsertIfNotExists("feed.boost.pro", "1.5", "PRO plan multiplier", "feed", "NUMBER");
        upsertIfNotExists("feed.boost.elite", "2.0", "ELITE plan multiplier", "feed", "NUMBER");
        upsertIfNotExists("feed.media.edit.hours", "2", "Hours allowed for media edit after post creation", "feed",
                "NUMBER");
        upsertIfNotExists("feed.base.freshness", "10", "Base freshness score for new posts", "feed", "NUMBER");

        // Ensure cache is synced after defaults
        init();
    }

    private void upsertIfNotExists(String key, String value, String description, String category, String valueType) {
        if (!configRepository.existsById(key)) {
            upsert(key, value, description, category, valueType);
        }
    }
}
