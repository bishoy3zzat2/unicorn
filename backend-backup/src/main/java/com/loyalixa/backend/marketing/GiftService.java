package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.GiftRequest;
import com.loyalixa.backend.marketing.dto.GiftVoucherCreateRequest;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.user.UserRepository;
import com.loyalixa.backend.course.CourseRepository;
import com.loyalixa.backend.course.Course;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.loyalixa.backend.course.EnrollmentService;
import com.loyalixa.backend.course.EnrollmentSource;
import java.time.LocalDateTime;
import java.util.UUID;
@Service
public class GiftService {
    private final GiftVoucherRepository giftVoucherRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;
    public GiftService(GiftVoucherRepository giftVoucherRepository, UserRepository userRepository, CourseRepository courseRepository, EnrollmentService enrollmentService) {
        this.giftVoucherRepository = giftVoucherRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.enrollmentService = enrollmentService;
    }
    @Transactional
    public GiftVoucher processGiftPurchase(GiftRequest request, User sender) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found for gifting."));
        if (sender.getEmail().equalsIgnoreCase(request.recipientEmail())) {
            throw new IllegalStateException("You cannot gift a course to your own email address.");
        }
        String voucherCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();  
        GiftVoucher voucher = new GiftVoucher();
        voucher.setVoucherCode(voucherCode);
        voucher.setRecipientEmail(request.recipientEmail().toLowerCase().trim());
        voucher.setCourse(course);
        voucher.setSender(sender);
        voucher.setStatus("ISSUED"); 
        GiftVoucher savedVoucher = giftVoucherRepository.save(voucher);
        System.out.println("--- GIFT VOUCHER ISSUED ---");
        System.out.println("Voucher: " + savedVoucher.getVoucherCode());
        System.out.println("To: " + savedVoucher.getRecipientEmail());
        System.out.println("---------------------------");
        return savedVoucher;
    }
    @Transactional
    public void redeemVoucher(String voucherCode, User redeemer) {
        GiftVoucher voucher = giftVoucherRepository.findByVoucherCodeAndStatus(voucherCode, "ISSUED")
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired voucher code."));
        if (voucher.getExpirationDate() != null && LocalDateTime.now().isAfter(voucher.getExpirationDate())) {
            voucher.setStatus("EXPIRED");
            giftVoucherRepository.save(voucher);
            throw new IllegalStateException("This voucher has expired.");
        }
        if (!voucher.getRecipientEmail().equalsIgnoreCase(redeemer.getEmail())) {
             throw new IllegalStateException("This voucher is registered to a different email address.");
        }
        enrollmentService.enrollUserInCourse(redeemer, voucher.getCourse(), EnrollmentSource.GIFT);  
        voucher.setStatus("REDEEMED");
        voucher.setRedeemedAt(LocalDateTime.now());
        voucher.setRedeemer(redeemer);
        giftVoucherRepository.save(voucher);
    }
    @Transactional
    public GiftVoucher createGiftVoucher(GiftVoucherCreateRequest request) {
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found."));
        User sender = userRepository.findById(request.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender user not found. The sender must be a registered user in the system."));
        String voucherCode;
        if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
            String requestedCode = request.voucherCode().trim().toUpperCase();
            if (giftVoucherRepository.findByVoucherCode(requestedCode).isPresent()) {
                throw new IllegalStateException("Voucher code '" + requestedCode + "' already exists. Please use a different code.");
            }
            voucherCode = requestedCode;
        } else {
            voucherCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            while (giftVoucherRepository.findByVoucherCode(voucherCode).isPresent()) {
                voucherCode = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            }
        }
        String status = (request.status() != null && !request.status().trim().isEmpty()) 
                ? request.status().trim().toUpperCase() 
                : "ISSUED";
        if (!status.equals("ISSUED") && !status.equals("REDEEMED") && !status.equals("EXPIRED")) {
            status = "ISSUED";
        }
        GiftVoucher voucher = new GiftVoucher();
        voucher.setVoucherCode(voucherCode);
        voucher.setRecipientEmail(request.recipientEmail().toLowerCase().trim());
        voucher.setCourse(course);
        voucher.setSender(sender);
        voucher.setStatus(status);
        if (request.description() != null && !request.description().trim().isEmpty()) {
            voucher.setDescription(request.description().trim());
        }
        if (request.expirationDate() != null) {
            voucher.setExpirationDate(request.expirationDate());
        }
        GiftVoucher savedVoucher = giftVoucherRepository.save(voucher);
        System.out.println("--- ADMIN GIFT VOUCHER CREATED ---");
        System.out.println("Voucher: " + savedVoucher.getVoucherCode());
        System.out.println("To: " + savedVoucher.getRecipientEmail());
        System.out.println("Status: " + savedVoucher.getStatus());
        System.out.println("----------------------------------");
        return savedVoucher;
    }
    @Transactional
    public GiftVoucher updateGiftVoucher(UUID voucherId, com.loyalixa.backend.marketing.dto.GiftVoucherUpdateRequest request) {
        GiftVoucher voucher = giftVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Gift voucher not found."));
        if ("REDEEMED".equals(voucher.getStatus())) {
            throw new IllegalStateException("Cannot update a voucher that has already been redeemed.");
        }
        if (request.courseId() != null) {
            Course course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new IllegalArgumentException("Course not found."));
            voucher.setCourse(course);
        }
        if (request.senderId() != null) {
            User sender = userRepository.findById(request.senderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender user not found."));
            voucher.setSender(sender);
        }
        if (request.recipientEmail() != null && !request.recipientEmail().trim().isEmpty()) {
            voucher.setRecipientEmail(request.recipientEmail().toLowerCase().trim());
        }
        if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
            String newCode = request.voucherCode().trim().toUpperCase();
            if (!newCode.equals(voucher.getVoucherCode())) {
                if (giftVoucherRepository.findByVoucherCode(newCode).isPresent()) {
                    throw new IllegalStateException("Voucher code '" + newCode + "' already exists. Please use a different code.");
                }
                voucher.setVoucherCode(newCode);
            }
        }
        if (request.status() != null && !request.status().trim().isEmpty()) {
            String newStatus = request.status().trim().toUpperCase();
            if (newStatus.equals("ISSUED") || newStatus.equals("REDEEMED") || newStatus.equals("EXPIRED")) {
                voucher.setStatus(newStatus);
                if ("REDEEMED".equals(newStatus) && voucher.getRedeemedAt() == null) {
                    voucher.setRedeemedAt(java.time.LocalDateTime.now());
                }
            }
        }
        if (request.description() != null) {
            if (request.description().trim().isEmpty()) {
                voucher.setDescription(null);
            } else {
                voucher.setDescription(request.description().trim());
            }
        }
        if (request.expirationDate() != null) {
            voucher.setExpirationDate(request.expirationDate());
        } else if (request.expirationDate() == null && voucher.getExpirationDate() != null) {
        }
        GiftVoucher updatedVoucher = giftVoucherRepository.save(voucher);
        System.out.println("--- ADMIN GIFT VOUCHER UPDATED ---");
        System.out.println("Voucher: " + updatedVoucher.getVoucherCode());
        System.out.println("Status: " + updatedVoucher.getStatus());
        System.out.println("----------------------------------");
        return updatedVoucher;
    }
    @Transactional
    public void deleteGiftVoucher(UUID voucherId) {
        GiftVoucher voucher = giftVoucherRepository.findById(voucherId)
                .orElseThrow(() -> new IllegalArgumentException("Gift voucher not found."));
        if ("REDEEMED".equals(voucher.getStatus())) {
            throw new IllegalStateException("Cannot delete a voucher that has already been redeemed.");
        }
        giftVoucherRepository.delete(voucher);
        System.out.println("--- ADMIN GIFT VOUCHER DELETED ---");
        System.out.println("Voucher: " + voucher.getVoucherCode());
        System.out.println("Status: " + voucher.getStatus());
        System.out.println("----------------------------------");
    }
}