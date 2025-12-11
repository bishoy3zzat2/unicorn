package com.loyalixa.backend.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SuspensionBanScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SuspensionBanScheduler.class);
    
    private final UserAdminService userAdminService;

    public SuspensionBanScheduler(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    // Check every 5 minutes instead of every hour for faster response
    // Cron format: "0 */5 * * * ?" = every 5 minutes
    // For testing every minute: "0 * * * * ?"
    // For testing every 30 seconds: "*/30 * * * * ?" (requires fixedDelayString instead)
    @Scheduled(cron = "0 */5 * * * ?")
    public void checkExpiredSuspensionsAndBans() {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            logger.info("[SuspensionBanScheduler] ⏰ Scheduled task running at: {}", now);
            System.out.println("[SuspensionBanScheduler] ⏰ Scheduled task running at: " + now);
            
            java.util.Map<String, Object> result = userAdminService.checkAndReactivateExpiredSuspensionsAndBans();
            int reactivatedCount = (Integer) result.get("reactivatedCount");
            @SuppressWarnings("unchecked")
            java.util.List<java.util.Map<String, Object>> reactivatedUsers = (java.util.List<java.util.Map<String, Object>>) result.get("reactivatedUsers");
            
            if (reactivatedCount > 0) {
                logger.info("[SuspensionBanScheduler] ✅ Reactivated {} user(s) with expired suspensions/bans", reactivatedCount);
                System.out.println("[SuspensionBanScheduler] ✅ Reactivated " + reactivatedCount + " user(s) with expired suspensions/bans");
                for (java.util.Map<String, Object> userInfo : reactivatedUsers) {
                    String action = (String) userInfo.get("action");
                    String email = (String) userInfo.get("email");
                    String reason = (String) userInfo.get("reason");
                    Object untilDate = userInfo.get(action.equals("SUSPENDED") ? "suspendedUntil" : "bannedUntil");
                    logger.info("[SuspensionBanScheduler]   - {}: {} (Reason: {}, Until: {})", action, email, reason, untilDate);
                    System.out.println("[SuspensionBanScheduler]   - " + action + ": " + email + " (Reason: " + reason + ", Until: " + untilDate + ")");
                }
            } else {
                logger.debug("[SuspensionBanScheduler] No expired suspensions/bans found");
                System.out.println("[SuspensionBanScheduler] No expired suspensions/bans found");
            }
        } catch (Exception e) {
            logger.error("[SuspensionBanScheduler] ❌ Error while checking expired suspensions/bans", e);
            System.err.println("[SuspensionBanScheduler] ❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
