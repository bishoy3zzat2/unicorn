package com.unicorn.backend.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request DTO for resolving a report.
 */
public record ResolveReportRequest(
                @NotNull(message = "Admin action is required") AdminAction adminAction,

                @Size(max = 2000, message = "Action details must not exceed 2000 characters") String actionDetails,

                @Size(max = 2000, message = "Admin notes must not exceed 2000 characters") String adminNotes,

                Boolean notifyReporter,
                List<NotificationChannel> reporterNotificationChannels,

                Boolean notifyReportedEntity,
                List<NotificationChannel> reportedEntityNotificationChannels) {
}
