    package com.loyalixa.backend.marketing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api/v1/admin/newsletter")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminNewsletterController {
    private final NewsletterService newsletterService;
    public AdminNewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }
    @GetMapping("/subscribers")
    public Page<com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active
    ){
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        return newsletterService.listSubscribers(search, active, pageable)
                .map(com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse::fromEntity);
    }
    @GetMapping("/subscribers/{id}")
    public com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse get(@PathVariable Long id){
        return com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse.fromEntity(newsletterService.getById(id));
    }
    @PostMapping("/subscribers")
    public ResponseEntity<?> create(@RequestParam String email, @org.springframework.security.core.annotation.AuthenticationPrincipal com.loyalixa.backend.user.User actor){
        try {
            NewsletterService.CreateOrActivateResult result = newsletterService.createOrActivateByAdmin(email, actor);
            Map<String, Object> body = new HashMap<>();
            body.put("status", result.reactivated ? "REACTIVATED" : "CREATED");
            body.put("message", result.reactivated ? "Email was inactive. It has been reactivated." : "Subscriber created successfully.");
            body.put("subscriber", result.subscriber);
            return ResponseEntity.status(result.reactivated ? 200 : 201).body(body);
        } catch (IllegalStateException ex) {
            if ("EXISTS_ACTIVE".equals(ex.getMessage())) {
                Map<String, Object> body = new HashMap<>();
                body.put("status", "ALREADY_ACTIVE");
                body.put("message", "Email is already subscribed and active.");
                return ResponseEntity.status(409).body(body);
            }
            throw ex;
        }
    }
    @PatchMapping("/subscribers/{id}/status")
    public com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse setStatus(@PathVariable Long id, @RequestParam boolean active, @org.springframework.security.core.annotation.AuthenticationPrincipal com.loyalixa.backend.user.User actor){
        return com.loyalixa.backend.marketing.dto.NewsletterSubscriberResponse.fromEntity(
                newsletterService.setActiveByAdmin(id, active, actor)
        );
    }
    @DeleteMapping("/subscribers/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        newsletterService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/stats")
    public Map<String, Object> stats(){
        long total = newsletterService.countAll();
        long active = newsletterService.countActive();
        long inactive = newsletterService.countInactive();
        Map<String, Object> m = new HashMap<>();
        m.put("total", total);
        m.put("active", active);
        m.put("inactive", inactive);
        m.put("today", 0);
        return m;
    }
}
