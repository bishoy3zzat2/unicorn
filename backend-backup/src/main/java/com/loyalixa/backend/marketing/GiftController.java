package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.GiftRequest;
import com.loyalixa.backend.user.User;
import com.loyalixa.backend.marketing.dto.RedeemRequest;  
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/gifts")
public class GiftController {
    private final GiftService giftService;
    public GiftController(GiftService giftService) {
        this.giftService = giftService;
    }
    @PostMapping("/purchase")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> purchaseGift(
            @Valid @RequestBody GiftRequest request, 
            @AuthenticationPrincipal User sender
    ) {
        try {
            GiftVoucher voucher = giftService.processGiftPurchase(request, sender);
            return ResponseEntity.status(HttpStatus.CREATED).body("Gift purchased. Code: " + voucher.getVoucherCode());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> redeemVoucher(
            @Valid @RequestBody RedeemRequest request, 
            @AuthenticationPrincipal User redeemer  
    ) {
        try {
            giftService.redeemVoucher(request.voucherCode(), redeemer);
            return ResponseEntity.ok("Voucher redeemed successfully! You are now enrolled in the course.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}