package com.loyalixa.backend.course.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
import java.util.List;
public record CourseProviderRequest(
    @NotBlank(message = "Provider name is required")
    @Size(max = 200, message = "Provider name must not exceed 200 characters")
    String name,
    @Size(max = 250, message = "Slug must not exceed 250 characters")
    String slug,  
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    String logoUrl,
    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    String websiteUrl,
    String description,  
    Boolean isActive,
    List<@Valid SocialLinkRequest> socialLinks  
) {}
