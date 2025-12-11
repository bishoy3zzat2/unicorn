package com.loyalixa.backend.marketing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, UUID> {
    Optional<EmailTemplate> findByTemplateType(String templateType);
    Optional<EmailTemplate> findByTemplateTypeAndIsActiveTrue(String templateType);
    boolean existsByTemplateType(String templateType);
}
