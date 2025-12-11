package com.loyalixa.backend.discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface DiscountCodeRepository extends JpaRepository<DiscountCode, UUID>, JpaSpecificationExecutor<DiscountCode> {
    Optional<DiscountCode> findByCodeIgnoreCase(String code);
    @Query(value = "SELECT d.id FROM discount_codes d " +
           "WHERE EXISTS (SELECT 1 FROM users u WHERE u.id = d.created_by_user_id)", nativeQuery = true)
    List<UUID> findIdsWithValidUsers();
    @Query("SELECT DISTINCT d FROM DiscountCode d " +
           "LEFT JOIN FETCH d.createdBy " +
           "LEFT JOIN FETCH d.updatedBy " +
           "LEFT JOIN FETCH d.applicableCourses " +
           "LEFT JOIN FETCH d.applicableProducts")
    List<DiscountCode> findAllWithRelations();
    @Query("SELECT DISTINCT d FROM DiscountCode d " +
           "LEFT JOIN FETCH d.createdBy " +
           "LEFT JOIN FETCH d.updatedBy " +
           "LEFT JOIN FETCH d.applicableCourses " +
           "LEFT JOIN FETCH d.applicableProducts " +
           "WHERE d.id = :id")
    Optional<DiscountCode> findByIdWithRelations(UUID id);
    @Query("SELECT d FROM DiscountCode d WHERE d.createdBy.id = :userId ORDER BY d.createdAt DESC")
    List<DiscountCode> findByCreatedByUserId(@Param("userId") UUID userId);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM discount_courses WHERE course_id = :courseId", nativeQuery = true)
    int deleteCourseLinks(@Param("courseId") UUID courseId);
}