package com.unicorn.backend.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Request DTO for suspending a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuspendUserRequest {
    private String reason;
    private boolean permanent;
    private Integer durationDays; // For quick options (7, 14, 30, etc.)
    private LocalDateTime expiresAt; // For custom date selection
}
