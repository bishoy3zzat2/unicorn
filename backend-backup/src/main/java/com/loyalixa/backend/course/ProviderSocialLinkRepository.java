package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
@Repository
public interface ProviderSocialLinkRepository extends JpaRepository<ProviderSocialLink, UUID> {
    List<ProviderSocialLink> findByProviderIdOrderByOrderIndexAsc(UUID providerId);
    void deleteByProviderId(UUID providerId);
}
