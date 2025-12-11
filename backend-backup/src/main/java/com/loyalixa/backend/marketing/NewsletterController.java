package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.SubscribeRequest;
import jakarta.validation.Valid;  
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/newsletter")
public class NewsletterController {
    private final NewsletterService newsletterService;
    public NewsletterController(NewsletterService newsletterService) {
        this.newsletterService = newsletterService;
    }
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscribeRequest request, HttpServletRequest httpRequest) {
        try {
            NewsletterSubscriber subscriber = newsletterService.subscribe(request, httpRequest);
            return new ResponseEntity<>(subscriber.getEmail(), HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
}