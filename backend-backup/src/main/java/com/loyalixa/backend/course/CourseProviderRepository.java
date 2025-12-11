package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface CourseProviderRepository extends JpaRepository<CourseProvider, UUID> {
    Optional<CourseProvider> findByNameIgnoreCase(String name);
    Optional<CourseProvider> findBySlug(String slug);
    @Query("SELECT p FROM CourseProvider p LEFT JOIN FETCH p.createdBy LEFT JOIN FETCH p.updatedBy WHERE p.id = :id")
    Optional<CourseProvider> findByIdWithUsers(@Param("id") UUID id);
    Boolean existsByName(String name);
    Boolean existsBySlug(String slug);
    List<CourseProvider> findByIsActiveTrue();
    List<CourseProvider> findAllByOrderByNameAsc();
}
