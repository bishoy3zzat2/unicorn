package com.loyalixa.backend.course.dto;
import com.loyalixa.backend.user.dto.UserPublicResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
public record CourseResponse(
    UUID id,
    String title,
    String slug,
    BigDecimal price,
    BigDecimal discountPrice,
    String level,
    String durationText,
    String coverImageUrl,
    List<UserPublicResponse> instructors
) {}