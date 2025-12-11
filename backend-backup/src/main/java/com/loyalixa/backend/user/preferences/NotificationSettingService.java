package com.loyalixa.backend.user.preferences;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class NotificationSettingService {
    private final UserNotificationSettingRepository notificationRepo;
    public NotificationSettingService(UserNotificationSettingRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }
    @Transactional(readOnly = true)
    public List<UserNotificationSetting> listForUser(UUID userId) {
        return notificationRepo.findAllByUserId(userId);
    }
    @Transactional
    public void setForUser(UUID userId, String preferenceType, boolean isEnabled) {
        UserNotificationSetting s = notificationRepo
                .findByUserIdAndPreferenceType(userId, preferenceType)
                .orElseGet(() -> {
                    UserNotificationSetting n = new UserNotificationSetting();
                    n.setUserId(userId);
                    n.setPreferenceType(preferenceType);
                    return n;
                });
        s.setEnabled(isEnabled);
        notificationRepo.save(s);
    }
    @Transactional
    public Map<String, Boolean> bulkSetForUser(UUID userId, Map<String, Boolean> updates) {
        if (updates == null || updates.isEmpty()) {
            return listForUser(userId).stream()
                    .collect(Collectors.toMap(UserNotificationSetting::getPreferenceType, UserNotificationSetting::isEnabled));
        }
        for (Map.Entry<String, Boolean> e : updates.entrySet()) {
            String type = e.getKey();
            Boolean enabled = e.getValue();
            if (type == null || type.isBlank() || enabled == null) {
                continue;
            }
            setForUser(userId, type.trim(), enabled);
        }
        return listForUser(userId).stream()
                .collect(Collectors.toMap(UserNotificationSetting::getPreferenceType, UserNotificationSetting::isEnabled));
    }
    @Transactional
    public void createDefaultsForUser(UUID userId) {
        List<String> defaultTypes = Arrays.asList(
                "MARKETING_PROMO",
                "SECURITY_ALERTS",
                "FOLLOWED_INSTRUCTOR_NEW_COURSE",
                "FOLLOWED_INSTRUCTOR_LIVE_SESSION",
                "COURSE_REMINDERS",
                "WEEKLY_SUMMARY"
        );
        for (String t : defaultTypes) {
            setForUser(userId, t, true);
        }
    }
}
