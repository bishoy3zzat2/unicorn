package com.unicorn.backend.nudge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for individual nudge info in admin dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NudgeInfoResponse {

        private Long id;
        private String senderId;
        private String senderName;
        private String senderEmail;
        private String senderAvatarUrl;
        private String receiverId;
        private String receiverName;
        private String receiverEmail;
        private String receiverAvatarUrl;

        // Startup info
        private String startupId;
        private String startupName;
        private String startupLogoUrl;
        private String startupIndustry;

        private LocalDateTime createdAt;

        public static NudgeInfoResponse fromEntity(Nudge nudge) {
                return NudgeInfoResponse.builder()
                                .id(nudge.getId())
                                .senderId(nudge.getSender().getId().toString())
                                .senderName(nudge.getSender().getFirstName() != null
                                                ? nudge.getSender().getFirstName() + " "
                                                                + nudge.getSender().getLastName()
                                                : nudge.getSender().getEmail().split("@")[0])
                                .senderEmail(nudge.getSender().getEmail())
                                .senderAvatarUrl(nudge.getSender().getAvatarUrl())
                                .receiverId(nudge.getReceiver().getId().toString())
                                .receiverName(nudge.getReceiver().getFirstName() != null
                                                ? nudge.getReceiver().getFirstName() + " "
                                                                + nudge.getReceiver().getLastName()
                                                : nudge.getReceiver().getEmail().split("@")[0])
                                .receiverEmail(nudge.getReceiver().getEmail())
                                .receiverAvatarUrl(nudge.getReceiver().getAvatarUrl())
                                .startupId(nudge.getStartup().getId().toString())
                                .startupName(nudge.getStartup().getName())
                                .startupLogoUrl(nudge.getStartup().getLogoUrl())
                                .startupIndustry(nudge.getStartup().getIndustry())
                                .createdAt(nudge.getCreatedAt())
                                .build();
        }
}
