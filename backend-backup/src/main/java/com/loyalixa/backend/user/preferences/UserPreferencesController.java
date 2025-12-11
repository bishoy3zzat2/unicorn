package com.loyalixa.backend.user.preferences;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.preferences.dto.UserPreferencesDtos.UserPreferencesResponse;
import com.loyalixa.backend.user.preferences.dto.UserPreferencesDtos.UserPreferencesUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/user/preferences")
public class UserPreferencesController {
    private final UserPreferenceService userPreferenceService;
    private final NotificationSettingService notificationSettingService;
    public UserPreferencesController(UserPreferenceService userPreferenceService,
                                     NotificationSettingService notificationSettingService) {
        this.userPreferenceService = userPreferenceService;
        this.notificationSettingService = notificationSettingService;
    }
    @GetMapping("/me")
    public ResponseEntity<UserPreferencesResponse> getMyPreferences(@AuthenticationPrincipal User currentUser) {
        UUID userId = currentUser.getId();
        UserPreference pref = userPreferenceService.getOrCreateDefaults(userId);
        Map<String, Boolean> notif = notificationSettingService.listForUser(userId).stream()
                .collect(Collectors.toMap(UserNotificationSetting::getPreferenceType, UserNotificationSetting::isEnabled));
        UserPreferencesResponse response = new UserPreferencesResponse(
                pref.getUiTheme(),
                pref.getUiLanguage(),
                pref.getTimezone(),
                notif
        );
        return ResponseEntity.ok(response);
    }
    @PutMapping("/me")
    public ResponseEntity<UserPreferencesResponse> updateMyPreferences(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UserPreferencesUpdateRequest request
    ) {
        UUID userId = currentUser.getId();
        UserPreference updatedUi = userPreferenceService.updateUiPreferences(
                userId,
                request.getUiTheme(),
                request.getUiLanguage(),
                request.getTimezone()
        );
        Map<String, Boolean> updatedNotif = notificationSettingService.bulkSetForUser(userId, request.getNotifications());
        UserPreferencesResponse response = new UserPreferencesResponse(
                updatedUi.getUiTheme(),
                updatedUi.getUiLanguage(),
                updatedUi.getTimezone(),
                updatedNotif
        );
        return ResponseEntity.ok(response);
    }
}
