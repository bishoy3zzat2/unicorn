package com.loyalixa.backend.messaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;
@Repository
public interface UserAlertRepository extends JpaRepository<UserAlert, UUID> {
    Page<UserAlert> findByRecipientIdOrderBySentAtDesc(UUID recipientId, Pageable pageable);
    long countByRecipientIdAndIsReadFalse(UUID recipientId);
    Optional<UserAlert> findByIdAndRecipientId(UUID alertId, UUID recipientId);
}