package com.unicorn.backend.startup;

import lombok.Data;

/**
 * DTO for updating startup status (admin use).
 */
@Data
public class UpdateStartupStatusRequest {
    private StartupStatus status;
    private String rejectionReason; // Optional, used when rejecting
}
