package com.loyalixa.backend.marketing.dto;
import com.loyalixa.backend.marketing.NewsletterSubscriber;
import com.loyalixa.backend.user.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
public class NewsletterSubscriberResponse {
    private Long id;
    private String email;
    private Boolean isActive;
    private LocalDateTime subscribedAt;
    private LocalDateTime updatedAt;
    private String userAgentRaw;
    private String browser;
    private String operatingSystem;
    private String deviceType;
    private String ipAddress;
    private String acceptLanguage;
    private String referrer;
    private String host;
    private String origin;
    private String acceptEncoding;
    private String dnt;
    private Integer screenWidth;
    private Integer screenHeight;
    private Integer viewportWidth;
    private Integer viewportHeight;
    private Double devicePixelRatio;
    private String timezone;
    private String platform;
    private Integer hardwareConcurrency;
    private Double deviceMemoryGb;
    private Boolean touchSupport;
    private UUID createdById;
    private String createdByEmail;
    private UUID updatedById;
    private String updatedByEmail;
    public static NewsletterSubscriberResponse fromEntity(NewsletterSubscriber e){
        NewsletterSubscriberResponse r = new NewsletterSubscriberResponse();
        if(e == null) return r;
        r.id = e.getId();
        r.email = e.getEmail();
        r.isActive = e.getIsActive();
        r.subscribedAt = e.getSubscribedAt();
        r.updatedAt = e.getUpdatedAt();
        r.userAgentRaw = e.getUserAgentRaw();
        r.browser = e.getBrowser();
        r.operatingSystem = e.getOperatingSystem();
        r.deviceType = e.getDeviceType();
        r.ipAddress = e.getIpAddress();
        r.acceptLanguage = e.getAcceptLanguage();
        r.referrer = e.getReferrer();
        r.host = e.getHost();
        r.origin = e.getOrigin();
        r.acceptEncoding = e.getAcceptEncoding();
        r.dnt = e.getDnt();
        r.screenWidth = e.getScreenWidth();
        r.screenHeight = e.getScreenHeight();
        r.viewportWidth = e.getViewportWidth();
        r.viewportHeight = e.getViewportHeight();
        r.devicePixelRatio = e.getDevicePixelRatio();
        r.timezone = e.getTimezone();
        r.platform = e.getPlatform();
        r.hardwareConcurrency = e.getHardwareConcurrency();
        r.deviceMemoryGb = e.getDeviceMemoryGb();
        r.touchSupport = e.getTouchSupport();
        User cb = e.getCreatedBy();
        if(cb != null){
            r.createdById = cb.getId();
            r.createdByEmail = cb.getEmail();
        }
        User ub = e.getUpdatedBy();
        if(ub != null){
            r.updatedById = ub.getId();
            r.updatedByEmail = ub.getEmail();
        }
        return r;
    }
}
