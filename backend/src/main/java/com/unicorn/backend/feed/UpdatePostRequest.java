package com.unicorn.backend.feed;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for updating an existing post.
 * Text is always editable, media only within 2 hours.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    /**
     * New media URL.
     * Will be rejected if post is older than 2 hours and already has media.
     */
    @Size(max = 500, message = "Media URL must not exceed 500 characters")
    private String mediaUrl;

    /**
     * Updated contextual title.
     */
    @Size(max = 200, message = "Contextual title must not exceed 200 characters")
    private String contextualTitle;

    private UUID contextualStartupId;
}
