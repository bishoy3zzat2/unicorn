package com.loyalixa.backend.marketing;
import com.loyalixa.backend.marketing.dto.SubscribeRequest;
import com.loyalixa.backend.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
@Service
public class NewsletterService {
    private final NewsletterSubscriberRepository subscriberRepository;
    public NewsletterService(NewsletterSubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }
    @Transactional
    public NewsletterSubscriber subscribe(SubscribeRequest request, HttpServletRequest http) {
        String email = request.email().toLowerCase().trim();
        if (subscriberRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email is already subscribed to the newsletter.");
        }
        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        subscriber.setIsActive(true); 
        populateClientMetadata(subscriber, http);
        populateClientProvidedMetadata(subscriber, request);
        return subscriberRepository.save(subscriber);
    }
    public static class CreateOrActivateResult {
        public final NewsletterSubscriber subscriber;
        public final boolean reactivated;
        public CreateOrActivateResult(NewsletterSubscriber subscriber, boolean reactivated){
            this.subscriber = subscriber;
            this.reactivated = reactivated;
        }
    }
    @Transactional
    public CreateOrActivateResult createOrActivate(String rawEmail){
        String email = rawEmail == null ? null : rawEmail.toLowerCase().trim();
        if(email == null || email.isEmpty()){
            throw new IllegalArgumentException("Email is required");
        }
        return subscriberRepository.findByEmail(email)
                .map(existing -> {
                    if(Boolean.TRUE.equals(existing.getIsActive())){
                        throw new IllegalStateException("EXISTS_ACTIVE");
                    }
                    existing.setIsActive(true);
                    NewsletterSubscriber saved = subscriberRepository.save(existing);
                    return new CreateOrActivateResult(saved, true);
                })
                .orElseGet(() -> {
                    NewsletterSubscriber s = new NewsletterSubscriber();
                    s.setEmail(email);
                    s.setIsActive(true);
                    NewsletterSubscriber saved = subscriberRepository.save(s);
                    return new CreateOrActivateResult(saved, false);
                });
    }
    @Transactional
    public CreateOrActivateResult createOrActivateByAdmin(String rawEmail, User actor){
        CreateOrActivateResult res = createOrActivate(rawEmail);
        NewsletterSubscriber sub = res.subscriber;
        if (sub != null && actor != null) {
            if (!res.reactivated && sub.getCreatedBy() == null) {
                sub.setCreatedBy(actor);
            }
            sub.setUpdatedBy(actor);
            sub = subscriberRepository.save(sub);
            sub = subscriberRepository.findById(sub.getId()).orElse(sub);
        }
        return new CreateOrActivateResult(sub, res.reactivated);
    }
    public Page<NewsletterSubscriber> listSubscribers(String search, Boolean active, Pageable pageable) {
        String s = (search == null ? "" : search).trim();
        boolean hasSearch = !s.isEmpty();
        String q = hasSearch ? s : null;
        if (active != null) {
            if (hasSearch) return subscriberRepository.findByEmailContainingIgnoreCaseAndIsActive(q, active, pageable);
            return subscriberRepository.findByIsActive(active, pageable);
        }
        if (hasSearch) return subscriberRepository.findByEmailContainingIgnoreCase(q, pageable);
        return subscriberRepository.findAll(pageable);
    }
    public NewsletterSubscriber getById(Long id) {
        return subscriberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Subscriber not found"));
    }
    @Transactional
    public NewsletterSubscriber create(String email) {
        SubscribeRequest req = new SubscribeRequest(
                email,
                null, null, null, null,  
                null,  
                null,  
                null,  
                null,  
                null,  
                null   
        );
        String e = req.email().toLowerCase().trim();
        NewsletterSubscriber s = new NewsletterSubscriber();
        s.setEmail(e);
        s.setIsActive(true);
        return subscriberRepository.save(s);
    }
    @Transactional
    public NewsletterSubscriber setActive(Long id, boolean active) {
        NewsletterSubscriber s = getById(id);
        s.setIsActive(active);
        return subscriberRepository.save(s);
    }
    @Transactional
    public NewsletterSubscriber setActiveByAdmin(Long id, boolean active, User actor) {
        NewsletterSubscriber s = setActive(id, active);
        if (actor != null) {
            s.setUpdatedBy(actor);
            s = subscriberRepository.save(s);
            s = subscriberRepository.findById(s.getId()).orElse(s);
        }
        return s;
    }
    @Transactional
    public void delete(Long id) {
        subscriberRepository.deleteById(id);
    }
    public long countAll() { return subscriberRepository.count(); }
    public long countActive() { return subscriberRepository.countByIsActive(true); }
    public long countInactive() { return subscriberRepository.countByIsActive(false); }
    private void populateClientMetadata(NewsletterSubscriber s, HttpServletRequest http){
        if (http == null || s == null) return;
        String ua = header(http, "User-Agent");
        s.setUserAgentRaw(ua);
        s.setAcceptLanguage(header(http, "Accept-Language"));
        s.setReferrer(header(http, "Referer"));
        s.setIpAddress(resolveClientIp(http));
        s.setHost(header(http, "Host"));
        s.setOrigin(header(http, "Origin"));
        s.setAcceptEncoding(header(http, "Accept-Encoding"));
        s.setDnt(header(http, "DNT"));  
        String os = "UNKNOWN";
        String browser = "UNKNOWN";
        String deviceType = "UNKNOWN";
        if (ua != null) {
            String u = ua.toLowerCase();
            if (u.contains("android")) os = "Android";
            else if (u.contains("iphone") || u.contains("ipad") || u.contains("ios")) os = "iOS";
            else if (u.contains("windows")) os = "Windows";
            else if (u.contains("mac os x") || u.contains("macintosh")) os = "macOS";
            else if (u.contains("linux")) os = "Linux";
            if (u.contains("edg/") || u.contains("edge/")) browser = "Edge";
            else if (u.contains("chrome/") && !u.contains("edg/")) browser = "Chrome";
            else if (u.contains("safari/") && !u.contains("chrome/")) browser = "Safari";
            else if (u.contains("firefox/")) browser = "Firefox";
            else if (u.contains("opera") || u.contains("opr/")) browser = "Opera";
            if (u.contains("mobile") || u.contains("iphone") || u.contains("android")) deviceType = "MOBILE";
            else if (u.contains("ipad") || u.contains("tablet")) deviceType = "TABLET";
            else if (u.contains("bot") || u.contains("spider") || u.contains("crawler")) deviceType = "BOT";
            else deviceType = "DESKTOP";
        }
        s.setOperatingSystem(os);
        s.setBrowser(browser);
        s.setDeviceType(deviceType);
    }
    private String header(HttpServletRequest http, String name){
        String v = http.getHeader(name);
        return v == null ? null : (v.length() > 0 ? v : null);
    }
    private String resolveClientIp(HttpServletRequest http){
        String[] candidates = new String[]{
                "X-Forwarded-For",
                "X-Real-IP",
                "CF-Connecting-IP"
        };
        for (String h : candidates){
            String v = http.getHeader(h);
            if (v != null && !v.isBlank()){
                int comma = v.indexOf(',');
                return comma > 0 ? v.substring(0, comma).trim() : v.trim();
            }
        }
        String remote = http.getRemoteAddr();
        return remote == null || remote.isBlank() ? null : remote;
    }
    private void populateClientProvidedMetadata(NewsletterSubscriber s, com.loyalixa.backend.marketing.dto.SubscribeRequest req){
        if (s == null || req == null) return;
        s.setScreenWidth(req.screenWidth());
        s.setScreenHeight(req.screenHeight());
        s.setViewportWidth(req.viewportWidth());
        s.setViewportHeight(req.viewportHeight());
        s.setDevicePixelRatio(req.devicePixelRatio());
        s.setTimezone(req.timezone());
        s.setPlatform(req.platform());
        s.setHardwareConcurrency(req.hardwareConcurrency());
        s.setDeviceMemoryGb(req.deviceMemoryGb());
        s.setTouchSupport(req.touchSupport());
    }
}