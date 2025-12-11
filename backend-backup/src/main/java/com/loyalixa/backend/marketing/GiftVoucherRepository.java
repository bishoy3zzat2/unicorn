package com.loyalixa.backend.marketing;
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
public interface GiftVoucherRepository extends JpaRepository<GiftVoucher, UUID>, JpaSpecificationExecutor<GiftVoucher> {
    Optional<GiftVoucher> findByVoucherCodeAndStatus(String voucherCode, String status);
    Optional<GiftVoucher> findByVoucherCode(String voucherCode);
    @Query("SELECT gv FROM GiftVoucher gv JOIN FETCH gv.course WHERE gv.sender.id = :senderId")
    List<GiftVoucher> findBySenderId(@Param("senderId") UUID senderId);
    @Query("SELECT gv FROM GiftVoucher gv JOIN FETCH gv.course LEFT JOIN FETCH gv.sender WHERE gv.redeemer.id = :redeemerId")
    List<GiftVoucher> findByRedeemerId(@Param("redeemerId") UUID redeemerId);
    @Query("SELECT gv FROM GiftVoucher gv JOIN FETCH gv.course WHERE gv.sender.id = :senderId")
    List<GiftVoucher> findAllBySenderId(@Param("senderId") UUID senderId);
    @Query("SELECT gv FROM GiftVoucher gv JOIN FETCH gv.course LEFT JOIN FETCH gv.sender WHERE LOWER(gv.recipientEmail) = LOWER(:email)")
    List<GiftVoucher> findByRecipientEmail(@Param("email") String email);
    @Query("SELECT gv FROM GiftVoucher gv JOIN FETCH gv.course JOIN FETCH gv.sender LEFT JOIN FETCH gv.redeemer WHERE gv.id = :id")
    Optional<GiftVoucher> findByIdWithRelations(@Param("id") UUID id);
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM gift_vouchers WHERE course_id = :courseId", nativeQuery = true)
    void deleteByCourseId(@Param("courseId") UUID courseId);
}