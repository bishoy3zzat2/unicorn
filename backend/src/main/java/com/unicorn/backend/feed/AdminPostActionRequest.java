package com.unicorn.backend.feed;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin actions on posts (hide, delete, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPostActionRequest {

    /**
     * Reason for the moderation action.
     */
    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
