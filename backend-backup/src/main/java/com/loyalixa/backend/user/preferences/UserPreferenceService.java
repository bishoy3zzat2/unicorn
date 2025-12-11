package com.loyalixa.backend.user.preferences;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
@Service
public class UserPreferenceService {
    private final UserPreferenceRepository userPreferenceRepository;
    public UserPreferenceService(UserPreferenceRepository userPreferenceRepository) {
        this.userPreferenceRepository = userPreferenceRepository;
    }
    @Transactional(readOnly = true)
    public UserPreference getOrCreateDefaults(UUID userId) {
        return userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultForUser(userId));
    }
    @Transactional
    public UserPreference createDefaultForUser(UUID userId) {
        if (userPreferenceRepository.existsByUserId(userId)) {
            return userPreferenceRepository.findByUserId(userId).orElseThrow();
        }
        UserPreference pref = new UserPreference();
        pref.setUserId(userId);
        return userPreferenceRepository.save(pref);
    }
    @Transactional
    public UserPreference updateUiPreferences(UUID userId, String uiTheme, String uiLanguage, String timezone) {
        UserPreference pref = userPreferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultForUser(userId));
        if (uiTheme != null && !uiTheme.isBlank()) {
            pref.setUiTheme(uiTheme.trim());
        }
        if (uiLanguage != null && !uiLanguage.isBlank()) {
            pref.setUiLanguage(uiLanguage.trim());
        }
        if (timezone != null && !timezone.isBlank()) {
            pref.setTimezone(timezone.trim());
        }
        return userPreferenceRepository.save(pref);
    }
}
