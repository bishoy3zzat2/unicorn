package com.loyalixa.backend.messaging;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.messaging.dto.AlertRequest;  
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class UserAlertService {
    private final UserAlertRepository alertRepository;
    private final UserRepository userRepository;
    public UserAlertService(UserAlertRepository alertRepository, UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }
    @Transactional
    public UserAlert createAlert(AlertRequest request, User sender) {
        User recipient = userRepository.findById(request.recipientId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient user not found."));
        UserAlert alert = new UserAlert();
        alert.setRecipient(recipient);
        alert.setSender(sender);  
        alert.setSubject(request.subject());
        alert.setContent(request.content());
        alert.setAlertType(request.alertType());
        alert.setIsRead(false);  
        return alertRepository.save(alert);
    }
    @Transactional(readOnly = true)
    public Page<UserAlert> getAlertsByRecipient(UUID recipientId, Pageable pageable) {
        return alertRepository.findByRecipientIdOrderBySentAtDesc(recipientId, pageable);
    }
    @Transactional
    public UserAlert markAlertAsRead(UUID alertId, UUID recipientId) {
        UserAlert alert = alertRepository.findByIdAndRecipientId(alertId, recipientId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or access denied."));
        if (!alert.getIsRead()) {
            alert.setIsRead(true);
            alert.setReadAt(LocalDateTime.now());  
            return alertRepository.save(alert);
        }
        return alert;  
    }
}