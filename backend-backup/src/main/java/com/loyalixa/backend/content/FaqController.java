package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.FaqDetailsResponse;
import com.loyalixa.backend.content.dto.FaqRequest;
import com.loyalixa.backend.content.dto.FaqResponse;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/faqs")
public class FaqController {
    private final FaqService faqService;
    private final FaqRepository faqRepository;
    public FaqController(FaqService faqService, FaqRepository faqRepository) {
        this.faqService = faqService;
        this.faqRepository = faqRepository;
    }
    @GetMapping
    public ResponseEntity<List<FaqResponse>> getAllFaqs() {
        List<FaqResponse> faqs = faqService.getAllFaqsResponse();
        return ResponseEntity.ok(faqs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<FaqResponse> getFaq(@PathVariable Long id) {
        try {
            Faq faq = faqRepository.findByIdWithCreatedAndUpdatedBy(id)
                    .orElseThrow(() -> new IllegalArgumentException("Faq not found."));
            FaqResponse response = faqService.mapFaqToResponse(faq);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @GetMapping("/{id}/details")
    public ResponseEntity<FaqDetailsResponse> getFaqDetails(@PathVariable Long id) {
        try {
            FaqDetailsResponse details = faqService.getFaqDetails(id);
            return ResponseEntity.ok(details);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    @PostMapping
    @PreAuthorize("hasAuthority('faq:create')")
    public ResponseEntity<FaqResponse> createFaq(@Valid @RequestBody FaqRequest request,
            @AuthenticationPrincipal User adminUser) {
        try {
            Faq newFaq = faqService.createFaq(request, adminUser);
            Faq loadedFaq = faqRepository.findByIdWithCreatedAndUpdatedBy(newFaq.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load created FAQ."));
            FaqResponse response = faqService.mapFaqToResponse(loadedFaq);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('faq:update')")
    public ResponseEntity<FaqResponse> updateFaq(
            @PathVariable Long id,
            @Valid @RequestBody FaqRequest request,
            @AuthenticationPrincipal User adminUser) {
        try {
            Faq updatedFaq = faqService.updateFaq(id, request, adminUser);
            Faq loadedFaq = faqRepository.findByIdWithCreatedAndUpdatedBy(updatedFaq.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Failed to load updated FAQ."));
            FaqResponse response = faqService.mapFaqToResponse(loadedFaq);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('faq:delete')")
    public ResponseEntity<Void> deleteFaq(@PathVariable Long id) {
        try {
            faqService.deleteFaq(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}