package com.loyalixa.backend.user.preferences;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface UserNotificationSettingRepository extends JpaRepository<UserNotificationSetting, Long> {
    List<UserNotificationSetting> findAllByUserId(UUID userId);
    Optional<UserNotificationSetting> findByUserIdAndPreferenceType(UUID userId, String preferenceType);
    void deleteByUserIdAndPreferenceType(UUID userId, String preferenceType);
    void deleteByUserId(UUID userId);
}
