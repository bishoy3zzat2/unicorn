package com.loyalixa.backend.course;
import com.loyalixa.backend.course.dto.CourseProviderRequest;
import com.loyalixa.backend.course.dto.CourseProviderResponse;
import com.loyalixa.backend.course.dto.SocialLinkRequest;
import com.loyalixa.backend.course.dto.SocialLinkResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class CourseProviderService {
    private final CourseProviderRepository courseProviderRepository;
    private final ProviderSocialLinkRepository socialLinkRepository;
    public CourseProviderService(CourseProviderRepository courseProviderRepository, 
                                 ProviderSocialLinkRepository socialLinkRepository) {
        this.courseProviderRepository = courseProviderRepository;
        this.socialLinkRepository = socialLinkRepository;
    }
    private String generateSlug(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "";
        }
        return name.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s-]", "")  
                .replaceAll("\\s+", "-")  
                .replaceAll("-+", "-")  
                .replaceAll("^-|-$", "");  
    }
    private String generateUniqueSlug(String baseSlug) {
        String slug = baseSlug;
        int counter = 1;
        while (courseProviderRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }
    @Transactional
    public CourseProvider createProvider(CourseProviderRequest request, com.loyalixa.backend.user.User currentUser) {
        if (courseProviderRepository.existsByName(request.name())) {
            throw new IllegalStateException("Course provider with this name already exists.");
        }
        String slug = request.slug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.name());
        } else {
            slug = slug.trim().toLowerCase();
        }
        if (courseProviderRepository.existsBySlug(slug)) {
            slug = generateUniqueSlug(slug);
        }
        CourseProvider newProvider = new CourseProvider();
        newProvider.setName(request.name());
        newProvider.setSlug(slug);
        newProvider.setLogoUrl(request.logoUrl());
        newProvider.setWebsiteUrl(request.websiteUrl());
        newProvider.setDescription(request.description());
        newProvider.setIsActive(request.isActive() != null ? request.isActive() : true);
        newProvider.setCreatedBy(currentUser);  
        newProvider.setUpdatedBy(currentUser);  
        CourseProvider savedProvider = courseProviderRepository.save(newProvider);
        if (request.socialLinks() != null && !request.socialLinks().isEmpty()) {
            saveSocialLinks(savedProvider, request.socialLinks());
        }
        return savedProvider;
    }
    @Transactional
    public CourseProvider updateProvider(UUID providerId, CourseProviderRequest request, com.loyalixa.backend.user.User currentUser) {
        CourseProvider provider = courseProviderRepository.findByIdWithUsers(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Course provider not found."));
        if (!provider.getName().equals(request.name()) && 
            courseProviderRepository.existsByName(request.name())) {
            throw new IllegalStateException("Another course provider with this name already exists.");
        }
        String slug = request.slug();
        if (slug == null || slug.trim().isEmpty()) {
            slug = generateSlug(request.name());
        } else {
            slug = slug.trim().toLowerCase();
        }
        if (!provider.getSlug().equals(slug) && courseProviderRepository.existsBySlug(slug)) {
            slug = generateUniqueSlug(slug);
        }
        provider.setName(request.name());
        provider.setSlug(slug);
        provider.setLogoUrl(request.logoUrl());
        provider.setWebsiteUrl(request.websiteUrl());
        provider.setDescription(request.description());
        if (request.isActive() != null) {
            provider.setIsActive(request.isActive());
        }
        provider.setUpdatedBy(currentUser);  
        socialLinkRepository.deleteByProviderId(providerId);
        if (request.socialLinks() != null && !request.socialLinks().isEmpty()) {
            saveSocialLinks(provider, request.socialLinks());
        }
        return courseProviderRepository.save(provider);
    }
    @Transactional(readOnly = true)
    public List<CourseProviderResponse> getAllProvidersResponse() {
        List<CourseProvider> providers = courseProviderRepository.findAllByOrderByNameAsc();
        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public List<CourseProviderResponse> getActiveProvidersResponse() {
        List<CourseProvider> providers = courseProviderRepository.findByIsActiveTrue();
        return providers.stream()
                .map(this::mapToProviderResponse)
                .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public CourseProviderResponse getProviderResponse(UUID providerId) {
        CourseProvider provider = courseProviderRepository.findByIdWithUsers(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Course provider not found."));
        return mapToProviderResponse(provider);
    }
    @Transactional(readOnly = true)
    public CourseProviderResponse getProviderResponseBySlug(String slug) {
        CourseProvider provider = courseProviderRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Course provider not found."));
        if (provider.getCreatedBy() != null && provider.getCreatedBy().getId() != null) {
            provider = courseProviderRepository.findByIdWithUsers(provider.getId())
                    .orElse(provider);
        }
        return mapToProviderResponse(provider);
    }
    @Transactional
    public void deleteProvider(UUID providerId) {
        CourseProvider provider = courseProviderRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Course provider not found."));
        courseProviderRepository.delete(provider);
    }
    private void saveSocialLinks(CourseProvider provider, List<SocialLinkRequest> socialLinkRequests) {
        int orderIndex = 0;
        for (SocialLinkRequest linkRequest : socialLinkRequests) {
            ProviderSocialLink link = new ProviderSocialLink();
            link.setProvider(provider);
            link.setPlatform(linkRequest.platform());
            link.setIconClass(linkRequest.iconClass());
            link.setUrl(linkRequest.url());
            link.setUsername(linkRequest.username());
            link.setIsUsernameBased(linkRequest.isUsernameBased() != null ? linkRequest.isUsernameBased() : false);
            link.setDisplayText(linkRequest.displayText());
            link.setOrderIndex(linkRequest.orderIndex() != null ? linkRequest.orderIndex() : orderIndex++);
            socialLinkRepository.save(link);
        }
    }
    private CourseProviderResponse mapToProviderResponse(CourseProvider provider) {
        List<ProviderSocialLink> links = socialLinkRepository.findByProviderIdOrderByOrderIndexAsc(provider.getId());
        List<SocialLinkResponse> socialLinks = links.stream()
                .map(link -> new SocialLinkResponse(
                        link.getId(),
                        link.getPlatform(),
                        link.getIconClass(),
                        link.getUrl(),
                        link.getUsername(),
                        link.getIsUsernameBased(),
                        link.getDisplayText(),
                        link.getOrderIndex(),
                        link.getCreatedAt(),
                        link.getUpdatedAt()
                ))
                .collect(Collectors.toList());
        CourseProviderResponse.UserInfo createdByInfo = null;
        if (provider.getCreatedBy() != null) {
            createdByInfo = new CourseProviderResponse.UserInfo(
                    provider.getCreatedBy().getId(),
                    provider.getCreatedBy().getUsername(),
                    provider.getCreatedBy().getEmail()
            );
        }
        CourseProviderResponse.UserInfo updatedByInfo = null;
        if (provider.getUpdatedBy() != null) {
            updatedByInfo = new CourseProviderResponse.UserInfo(
                    provider.getUpdatedBy().getId(),
                    provider.getUpdatedBy().getUsername(),
                    provider.getUpdatedBy().getEmail()
            );
        }
        return new CourseProviderResponse(
                provider.getId(),
                provider.getName(),
                provider.getSlug(),
                provider.getLogoUrl(),
                provider.getWebsiteUrl(),
                provider.getDescription(),
                provider.getIsActive(),
                socialLinks,
                createdByInfo,
                updatedByInfo,
                provider.getCreatedAt(),
                provider.getUpdatedAt()
        );
    }
}
