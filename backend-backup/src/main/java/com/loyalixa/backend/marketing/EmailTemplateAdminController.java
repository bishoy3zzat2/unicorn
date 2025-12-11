package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.EmailTemplateRequest;
import com.loyalixa.backend.marketing.dto.EmailTemplateResponse;
import com.loyalixa.backend.user.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/v1/admin/emails/templates")
public class EmailTemplateAdminController {
    private final EmailTemplateService emailTemplateService;
    public EmailTemplateAdminController(EmailTemplateService emailTemplateService) {
        this.emailTemplateService = emailTemplateService;
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:view')")
    public ResponseEntity<Page<EmailTemplateResponse>> getAllTemplates(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search
    ) {
        Page<EmailTemplateResponse> templates = emailTemplateService.getAllTemplates(page, size, search);
        return ResponseEntity.ok(templates);
    }
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:view')")
    public ResponseEntity<List<EmailTemplateResponse>> getAllTemplatesList() {
        List<EmailTemplateResponse> templates = emailTemplateService.getAllTemplatesList();
        return ResponseEntity.ok(templates);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:view')")
    public ResponseEntity<EmailTemplateResponse> getTemplateById(@PathVariable UUID id) {
        EmailTemplateResponse template = emailTemplateService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }
    @GetMapping("/type/{templateType}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:view')")
    public ResponseEntity<EmailTemplateResponse> getTemplateByType(@PathVariable String templateType) {
        EmailTemplateResponse template = emailTemplateService.getTemplateByType(templateType);
        return ResponseEntity.ok(template);
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:create')")
    public ResponseEntity<?> createTemplate(
            @Valid @RequestBody EmailTemplateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            EmailTemplateResponse template = emailTemplateService.createTemplate(request, currentUser);
            return new ResponseEntity<>(template, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:update')")
    public ResponseEntity<?> updateTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody EmailTemplateRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        try {
            EmailTemplateResponse template = emailTemplateService.updateTemplate(id, request, currentUser);
            return ResponseEntity.ok(template);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('email:delete')")
    public ResponseEntity<Void> deleteTemplate(@PathVariable UUID id) {
        try {
            emailTemplateService.deleteTemplate(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
