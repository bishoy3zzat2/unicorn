package com.loyalixa.backend.content;
import com.loyalixa.backend.content.dto.FaqDetailsResponse;
import com.loyalixa.backend.content.dto.FaqRequest;
import com.loyalixa.backend.content.dto.FaqResponse;
import com.loyalixa.backend.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class FaqService {
    private final FaqRepository faqRepository;
    public FaqService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }
    @Transactional
    public Faq createFaq(FaqRequest request, User adminUser) {
        Faq faq = new Faq();
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setCategory(request.category());
        faq.setOrderIndex(request.orderIndex());
        faq.setCreatedBy(adminUser);
        return faqRepository.save(faq);
    }
    @Transactional
    public Faq createFaq(FaqRequest request) {
        return createFaq(request, null);
    }
    @Transactional(readOnly = true)
    public List<Faq> getAllFaqs() {
        return faqRepository.findAll();
    }
    @Transactional(readOnly = true)
    public List<Faq> getAllFaqsWithAuditing() {
        return faqRepository.findAllWithCreatedAndUpdatedBy();
    }
    @Transactional(readOnly = true)
    public List<FaqResponse> getAllFaqsResponse() {
        List<Faq> faqs = faqRepository.findAllWithCreatedAndUpdatedBy();
        return faqs.stream()
                .map(this::mapToFaqResponse)
                .collect(Collectors.toList());
    }
    private FaqResponse mapToFaqResponse(Faq faq) {
        FaqResponse.UserInfo createdByInfo = null;
        if (faq.getCreatedBy() != null) {
            var createdBy = faq.getCreatedBy();
            createdByInfo = new FaqResponse.UserInfo(
                    createdBy.getId(),
                    createdBy.getEmail(),
                    createdBy.getUsername());
        }
        FaqResponse.UserInfo updatedByInfo = null;
        if (faq.getUpdatedBy() != null) {
            var updatedBy = faq.getUpdatedBy();
            updatedByInfo = new FaqResponse.UserInfo(
                    updatedBy.getId(),
                    updatedBy.getEmail(),
                    updatedBy.getUsername());
        }
        return new FaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory(),
                faq.getOrderIndex(),
                faq.getCreatedAt(),
                faq.getUpdatedAt(),
                createdByInfo,
                updatedByInfo);
    }
    @Transactional
    public Faq updateFaq(Long id, FaqRequest request, User adminUser) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faq not found"));
        faq.setQuestion(request.question());
        faq.setAnswer(request.answer());
        faq.setCategory(request.category());
        faq.setOrderIndex(request.orderIndex());
        faq.setUpdatedBy(adminUser);
        return faqRepository.save(faq);
    }
    @Transactional
    public Faq updateFaq(Long id, FaqRequest request) {
        return updateFaq(id, request, null);
    }
    @Transactional(readOnly = true)
    public FaqResponse mapFaqToResponse(Faq faq) {
        return mapToFaqResponse(faq);
    }
    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new IllegalArgumentException("Faq not found");
        }
        faqRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public Faq getFaqById(Long id) {
        return faqRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Faq not found."));
    }
    @Transactional(readOnly = true)
    public FaqDetailsResponse getFaqDetails(Long id) {
        Faq faq = faqRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new IllegalArgumentException("Faq not found."));
        FaqDetailsResponse.UserInfo createdByInfo = null;
        if (faq.getCreatedBy() != null) {
            User createdBy = faq.getCreatedBy();
            createdByInfo = new FaqDetailsResponse.UserInfo(
                    createdBy.getId(),
                    createdBy.getEmail(),
                    createdBy.getUsername(),
                    createdBy.getRole() != null ? createdBy.getRole().getName() : null);
        }
        FaqDetailsResponse.UserInfo updatedByInfo = null;
        if (faq.getUpdatedBy() != null) {
            User updatedBy = faq.getUpdatedBy();
            updatedByInfo = new FaqDetailsResponse.UserInfo(
                    updatedBy.getId(),
                    updatedBy.getEmail(),
                    updatedBy.getUsername(),
                    updatedBy.getRole() != null ? updatedBy.getRole().getName() : null);
        }
        return new FaqDetailsResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory(),
                faq.getOrderIndex(),
                createdByInfo,
                updatedByInfo,
                faq.getCreatedAt(),
                faq.getUpdatedAt());
    }
}