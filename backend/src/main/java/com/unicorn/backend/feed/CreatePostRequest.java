package com.unicorn.backend.feed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new post.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    @Size(max = 500, message = "Media URL must not exceed 500 characters")
    private String mediaUrl;

    /**
     * Optional startup ID for contextual title.
     * If provided, backend validates user membership and auto-generates title.
     * Example: User owns "Unicorn App" → "Founder at Unicorn App"
     */
    private UUID contextualStartupId;
}
