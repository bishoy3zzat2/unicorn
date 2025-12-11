package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.EmailTemplateRequest;
import com.loyalixa.backend.marketing.dto.EmailTemplateResponse;
import com.loyalixa.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class EmailTemplateService {
    private final EmailTemplateRepository emailTemplateRepository;
    public EmailTemplateService(EmailTemplateRepository emailTemplateRepository) {
        this.emailTemplateRepository = emailTemplateRepository;
    }
    @Transactional(readOnly = true)
    public Page<EmailTemplateResponse> getAllTemplates(int page, int size, String search) {
        Pageable pageable = PageRequest.of(page, size);
        Page<EmailTemplate> templates = emailTemplateRepository.findAll(pageable);
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            List<EmailTemplate> filtered = templates.getContent().stream()
                .filter(t -> t.getTemplateType().toLowerCase().contains(searchLower) ||
                           (t.getSubject() != null && t.getSubject().toLowerCase().contains(searchLower)))
                .collect(Collectors.toList());
            Pageable filteredPageable = PageRequest.of(page, size);
            int start = (int) filteredPageable.getOffset();
            int end = Math.min((start + filteredPageable.getPageSize()), filtered.size());
            List<EmailTemplate> pagedFiltered = filtered.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(
                pagedFiltered.stream().map(this::mapToResponse).collect(Collectors.toList()),
                filteredPageable,
                filtered.size()
            );
        }
        return templates.map(this::mapToResponse);
    }
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllTemplatesList() {
        return emailTemplateRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateById(UUID id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Email template not found: " + id));
        return mapToResponse(template);
    }
    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateByType(String templateType) {
        EmailTemplate template = emailTemplateRepository.findByTemplateTypeAndIsActiveTrue(templateType)
            .orElseThrow(() -> new IllegalArgumentException("Email template not found for type: " + templateType));
        return mapToResponse(template);
    }
    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request, User createdBy) {
        if (emailTemplateRepository.existsByTemplateType(request.templateType())) {
            throw new IllegalStateException("Email template with type '" + request.templateType() + "' already exists.");
        }
        EmailTemplate template = new EmailTemplate();
        template.setTemplateType(request.templateType().toUpperCase());
        template.setSubject(request.subject());
        template.setHtmlContent(request.htmlContent());
        template.setTextContent(request.textContent());
        template.setDescription(request.description());
        template.setIsActive(request.isActive() != null ? request.isActive() : true);
        template.setCreatedBy(createdBy);
        EmailTemplate saved = emailTemplateRepository.save(template);
        return mapToResponse(saved);
    }
    @Transactional
    public EmailTemplateResponse updateTemplate(UUID id, EmailTemplateRequest request, User updatedBy) {
        EmailTemplate template = emailTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Email template not found: " + id));
        if (!template.getTemplateType().equalsIgnoreCase(request.templateType())) {
            if (emailTemplateRepository.existsByTemplateType(request.templateType())) {
                throw new IllegalStateException("Email template with type '" + request.templateType() + "' already exists.");
            }
            template.setTemplateType(request.templateType().toUpperCase());
        }
        template.setSubject(request.subject());
        template.setHtmlContent(request.htmlContent());
        template.setTextContent(request.textContent());
        template.setDescription(request.description());
        if (request.isActive() != null) {
            template.setIsActive(request.isActive());
        }
        template.setUpdatedBy(updatedBy);
        EmailTemplate updated = emailTemplateRepository.save(template);
        return mapToResponse(updated);
    }
    @Transactional
    public void deleteTemplate(UUID id) {
        if (!emailTemplateRepository.existsById(id)) {
            throw new IllegalArgumentException("Email template not found: " + id);
        }
        emailTemplateRepository.deleteById(id);
    }
    private EmailTemplateResponse mapToResponse(EmailTemplate template) {
        return new EmailTemplateResponse(
            template.getId(),
            template.getTemplateType(),
            template.getSubject(),
            template.getHtmlContent(),
            template.getTextContent(),
            template.getDescription(),
            template.getIsActive(),
            template.getCreatedBy() != null ? template.getCreatedBy().getId() : null,
            template.getCreatedBy() != null ? template.getCreatedBy().getUsername() : null,
            template.getUpdatedBy() != null ? template.getUpdatedBy().getId() : null,
            template.getUpdatedBy() != null ? template.getUpdatedBy().getUsername() : null,
            template.getCreatedAt(),
            template.getUpdatedAt()
        );
    }
}
