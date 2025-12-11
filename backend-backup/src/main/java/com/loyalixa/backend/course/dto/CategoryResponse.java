package com.loyalixa.backend.course.dto;
public record CategoryResponse(
    Long id,
    String name,
    String slug,
    String iconClass,
    Integer orderIndex
) {}
