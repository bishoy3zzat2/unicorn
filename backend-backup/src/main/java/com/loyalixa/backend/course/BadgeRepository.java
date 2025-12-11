package com.loyalixa.backend.course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID; 
@Repository
public interface BadgeRepository extends JpaRepository<Badge, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    Optional<Badge> findByNameIgnoreCase(String name);
    @Query(value = "SELECT COUNT(*) > 0 FROM badges b WHERE " +
           "LOWER(b.name) = LOWER(:name) AND " +
           "(COALESCE(TRIM(b.custom_css), '') = COALESCE(TRIM(COALESCE(:customCss, '')), '')) AND " +
           "(COALESCE(b.weight, 0.0) = COALESCE(:weight, 0.0)) AND " +
           "((b.expiration_date IS NULL AND :expirationDate IS NULL) OR " +
           " (b.expiration_date IS NOT NULL AND :expirationDate IS NOT NULL AND b.expiration_date = :expirationDate)) AND " +
           "((b.usage_duration IS NULL AND :usageDurationMinutes IS NULL) OR " +
           " (b.usage_duration IS NOT NULL AND :usageDurationMinutes IS NOT NULL AND " +
           "  EXTRACT(EPOCH FROM b.usage_duration) / 60 = :usageDurationMinutes)) AND " +
           "(COALESCE(UPPER(b.target_type), 'COURSE') = COALESCE(UPPER(COALESCE(:targetType, 'COURSE')), 'COURSE'))",
           nativeQuery = true)
    boolean existsByAllProperties(
        @Param("name") String name,
        @Param("customCss") String customCss,
        @Param("weight") Double weight,
        @Param("expirationDate") LocalDateTime expirationDate,
        @Param("usageDurationMinutes") Long usageDurationMinutes,
        @Param("targetType") String targetType
    );
    @Query(value = "SELECT COUNT(*) > 0 FROM badges b WHERE " +
           "b.id != :id AND " +
           "LOWER(b.name) = LOWER(:name) AND " +
           "(COALESCE(TRIM(b.custom_css), '') = COALESCE(TRIM(COALESCE(:customCss, '')), '')) AND " +
           "(COALESCE(b.weight, 0.0) = COALESCE(:weight, 0.0)) AND " +
           "((b.expiration_date IS NULL AND :expirationDate IS NULL) OR " +
           " (b.expiration_date IS NOT NULL AND :expirationDate IS NOT NULL AND b.expiration_date = :expirationDate)) AND " +
           "((b.usage_duration IS NULL AND :usageDurationMinutes IS NULL) OR " +
           " (b.usage_duration IS NOT NULL AND :usageDurationMinutes IS NOT NULL AND " +
           "  EXTRACT(EPOCH FROM b.usage_duration) / 60 = :usageDurationMinutes)) AND " +
           "(COALESCE(UPPER(b.target_type), 'COURSE') = COALESCE(UPPER(COALESCE(:targetType, 'COURSE')), 'COURSE'))",
           nativeQuery = true)
    boolean existsByAllPropertiesAndIdNot(
        @Param("id") UUID id,
        @Param("name") String name,
        @Param("customCss") String customCss,
        @Param("weight") Double weight,
        @Param("expirationDate") LocalDateTime expirationDate,
        @Param("usageDurationMinutes") Long usageDurationMinutes,
        @Param("targetType") String targetType
    );
    @Query("SELECT DISTINCT b FROM Badge b " +
           "LEFT JOIN FETCH b.createdBy cb " +
           "LEFT JOIN FETCH cb.role " +
           "LEFT JOIN FETCH b.updatedBy ub " +
           "LEFT JOIN FETCH ub.role " +
           "LEFT JOIN FETCH b.coursesUsingBadge cb_rel " +
           "LEFT JOIN FETCH cb_rel.course " +
           "WHERE b.id = :id")
    Optional<Badge> findByIdWithRelations(UUID id);
    @Query("SELECT DISTINCT b FROM Badge b " +
           "LEFT JOIN FETCH b.createdBy " +
           "LEFT JOIN FETCH b.updatedBy")
    List<Badge> findAllWithCreatedAndUpdatedBy();
    @Query("SELECT DISTINCT b FROM Badge b " +
           "LEFT JOIN FETCH b.createdBy " +
           "LEFT JOIN FETCH b.updatedBy " +
           "WHERE b.id = :id")
    Optional<Badge> findByIdWithCreatedAndUpdatedBy(UUID id);
}