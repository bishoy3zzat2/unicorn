package com.loyalixa.backend.content.dto;
import jakarta.validation.constraints.NotBlank;
public record PartnerRequest(
    @NotBlank
    String name,
    @NotBlank
    String logoUrl,  
    String websiteUrl,
    Integer orderIndex
) {}