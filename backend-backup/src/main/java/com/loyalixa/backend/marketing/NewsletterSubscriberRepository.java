package com.loyalixa.backend.marketing;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    Optional<NewsletterSubscriber> findByEmail(String email);
    Boolean existsByEmail(String email);
    @Override
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    @NonNull
    Page<NewsletterSubscriber> findAll(@NonNull Pageable pageable);
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    Page<NewsletterSubscriber> findByEmailContainingIgnoreCase(String email, Pageable pageable);
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    Page<NewsletterSubscriber> findByIsActive(Boolean isActive, Pageable pageable);
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    Page<NewsletterSubscriber> findByEmailContainingIgnoreCaseAndIsActive(String email, Boolean isActive, Pageable pageable);
    @Override
    @EntityGraph(attributePaths = {"createdBy", "updatedBy"})
    @NonNull
    Optional<NewsletterSubscriber> findById(@NonNull Long id);
    long countByIsActive(Boolean isActive);
}