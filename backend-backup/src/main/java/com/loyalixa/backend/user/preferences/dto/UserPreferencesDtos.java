package com.loyalixa.backend.user.preferences.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
public class UserPreferencesDtos {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesResponse {
        private String uiTheme;
        private String uiLanguage;
        private String timezone;
        private Map<String, Boolean> notifications;
    }
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferencesUpdateRequest {
        private String uiTheme;
        private String uiLanguage;
        private String timezone;
        private Map<String, Boolean> notifications;
    }
}
