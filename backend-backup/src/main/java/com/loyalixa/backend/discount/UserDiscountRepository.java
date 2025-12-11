package com.loyalixa.backend.discount;
import com.loyalixa.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface UserDiscountRepository extends JpaRepository<UserDiscount, Long> {
    Optional<UserDiscount> findByUserIdAndDiscountCode(UUID userId, DiscountCode discountCode);
    @Query("SELECT ud FROM UserDiscount ud JOIN FETCH ud.user u JOIN FETCH u.role WHERE ud.discountCode = :discountCode")
    List<UserDiscount> findByDiscountCode(DiscountCode discountCode);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM UserDiscount ud WHERE ud.discountCode = :discountCode")
    void deleteByDiscountCode(DiscountCode discountCode);
    @Query("SELECT ud FROM UserDiscount ud JOIN FETCH ud.discountCode WHERE ud.user.id = :userId")
    List<UserDiscount> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT COUNT(ud) FROM UserDiscount ud WHERE ud.user.id = :userId AND ud.isUsed = true")
    Long countUsedByUserId(@Param("userId") UUID userId);
    @Query("SELECT COUNT(ud) FROM UserDiscount ud WHERE ud.user.id = :userId AND ud.isUsed = false")
    Long countUnusedByUserId(@Param("userId") UUID userId);
}