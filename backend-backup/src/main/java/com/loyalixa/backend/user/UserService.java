package com.loyalixa.backend.user;
import com.loyalixa.backend.security.Role;
import com.loyalixa.backend.security.RoleRepository;
import com.loyalixa.backend.user.preferences.NotificationSettingService;
import com.loyalixa.backend.user.preferences.UserPreferenceService;
import com.loyalixa.backend.subscription.SubscriptionPlanRepository;
import com.loyalixa.backend.subscription.SubscriptionPlan;
import com.loyalixa.backend.subscription.UserSubscription;
import com.loyalixa.backend.subscription.UserSubscriptionRepository;
import com.loyalixa.backend.lxcoins.LXCoinsAccount;
import com.loyalixa.backend.lxcoins.LXCoinsAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;
@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPreferenceService userPreferenceService;
    private final NotificationSettingService notificationSettingService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final LXCoinsAccountRepository lxCoinsAccountRepository;
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
            UserPreferenceService userPreferenceService,
            NotificationSettingService notificationSettingService,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            LXCoinsAccountRepository lxCoinsAccountRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPreferenceService = userPreferenceService;
        this.notificationSettingService = notificationSettingService;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.lxCoinsAccountRepository = lxCoinsAccountRepository;
    }
    @Transactional
    public User registerUser(String username, String email, String rawPassword,
            HttpServletRequest httpRequest,
            String deviceType, String timezone, String platform,
            Integer screenWidth, Integer screenHeight,
            Integer viewportWidth, Integer viewportHeight,
            Integer hardwareConcurrency, Double deviceMemory,
            Double devicePixelRatio, Boolean touchSupport) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalStateException("Error: Email is required!");
        }
        email = email.trim();
        if (username == null || username.trim().isEmpty()) {
            username = email;
        } else {
            username = username.trim();
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Error: Username is already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Error: Email is already in use!");
        }
        Role studentRole = roleRepository.findByName("STUDENT")
                .orElseThrow(() -> new IllegalStateException("Default role 'STUDENT' not found. Check initial data."));
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        String hashedPassword = passwordEncoder.encode(rawPassword);
        newUser.setPasswordHash(hashedPassword);
        newUser.setRole(studentRole);
        newUser.setStatus("ACTIVE");
        newUser.setAuthProvider("EMAIL");
        populateUserDeviceMetadata(newUser, httpRequest, deviceType, timezone, platform,
                screenWidth, screenHeight, viewportWidth, viewportHeight,
                hardwareConcurrency, deviceMemory, devicePixelRatio, touchSupport);
        User saved = userRepository.save(newUser);
        userPreferenceService.createDefaultForUser(saved.getId());
        notificationSettingService.createDefaultsForUser(saved.getId());
        createFreePlanSubscription(saved);
        createInactiveLXCoinsAccount(saved);
        return saved;
    }
    @Transactional
    public User registerUser(String username, String email, String rawPassword) {
        return registerUser(username, email, rawPassword, null, null, null, null,
                null, null, null, null, null, null, null, null);
    }
    private void populateUserDeviceMetadata(User user, HttpServletRequest httpRequest,
            String deviceType, String timezone, String platform,
            Integer screenWidth, Integer screenHeight,
            Integer viewportWidth, Integer viewportHeight,
            Integer hardwareConcurrency, Double deviceMemory,
            Double devicePixelRatio, Boolean touchSupport) {
        if (user == null)
            return;
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        String ipAddress = resolveClientIp(httpRequest);
        String acceptLanguage = httpRequest != null ? httpRequest.getHeader("Accept-Language") : null;
        String acceptEncoding = httpRequest != null ? httpRequest.getHeader("Accept-Encoding") : null;
        String dnt = httpRequest != null ? httpRequest.getHeader("DNT") : null;
        user.setUserAgent(userAgent);
        if (userAgent != null && !userAgent.isEmpty()) {
            user.setBrowser(extractBrowser(userAgent));
            user.setOperatingSystem(extractOperatingSystem(userAgent));
        }
        if (deviceType == null && httpRequest != null) {
            deviceType = extractDeviceType(httpRequest);
        }
        user.setDeviceType(deviceType);
        user.setIpAddress(ipAddress);
        user.setAcceptLanguage(acceptLanguage);
        user.setAcceptEncoding(acceptEncoding);
        user.setDnt(dnt);
        String referrer = httpRequest != null ? httpRequest.getHeader("Referer") : null;
        String host = httpRequest != null ? httpRequest.getHeader("Host") : null;
        String origin = httpRequest != null ? httpRequest.getHeader("Origin") : null;
        user.setReferrer(referrer);
        user.setHost(host);
        user.setOrigin(origin);
        user.setTimezone(timezone);
        user.setPlatform(platform != null ? platform : deviceType);
        user.setScreenWidth(screenWidth);
        user.setScreenHeight(screenHeight);
        user.setViewportWidth(viewportWidth);
        user.setViewportHeight(viewportHeight);
        user.setDevicePixelRatio(devicePixelRatio);
        user.setHardwareConcurrency(hardwareConcurrency);
        user.setDeviceMemoryGb(deviceMemory);
        user.setTouchSupport(touchSupport);
    }
    private String extractDeviceType(HttpServletRequest request) {
        if (request == null)
            return "UNKNOWN";
        String ua = request.getHeader("User-Agent");
        if (ua == null || ua.isEmpty())
            return "UNKNOWN";
        String u = ua.toLowerCase();
        if (u.contains("mobile") || u.contains("iphone") || (u.contains("android") && !u.contains("tablet"))) {
            return "MOBILE";
        } else if (u.contains("ipad") || u.contains("tablet") || (u.contains("android") && u.contains("tablet"))) {
            return "TABLET";
        } else if (u.contains("bot") || u.contains("spider") || u.contains("crawler")) {
            return "BOT";
        } else {
            return "DESKTOP";
        }
    }
    private String extractBrowser(String userAgent) {
        if (userAgent == null || userAgent.isEmpty())
            return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("edg/") || ua.contains("edge/"))
            return "Edge";
        if (ua.contains("chrome/") && !ua.contains("edg/"))
            return "Chrome";
        if (ua.contains("safari/") && !ua.contains("chrome/"))
            return "Safari";
        if (ua.contains("firefox/"))
            return "Firefox";
        if (ua.contains("opera/") || ua.contains("opr/"))
            return "Opera";
        if (ua.contains("msie") || ua.contains("trident/"))
            return "Internet Explorer";
        return "UNKNOWN";
    }
    private String extractOperatingSystem(String userAgent) {
        if (userAgent == null || userAgent.isEmpty())
            return "UNKNOWN";
        String ua = userAgent.toLowerCase();
        if (ua.contains("windows nt 10.0"))
            return "Windows 10";
        if (ua.contains("windows nt 6.3"))
            return "Windows 8.1";
        if (ua.contains("windows nt 6.2"))
            return "Windows 8";
        if (ua.contains("windows nt 6.1"))
            return "Windows 7";
        if (ua.contains("windows"))
            return "Windows";
        if (ua.contains("mac os x") || ua.contains("macintosh"))
            return "macOS";
        if (ua.contains("android"))
            return "Android";
        if (ua.contains("iphone") || ua.contains("ipad") || ua.contains("ios"))
            return "iOS";
        if (ua.contains("linux"))
            return "Linux";
        if (ua.contains("ubuntu"))
            return "Ubuntu";
        if (ua.contains("fedora"))
            return "Fedora";
        return "UNKNOWN";
    }
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null)
            return null;
        String[] headers = { "X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP" };
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.trim().isEmpty()) {
                int comma = value.indexOf(',');
                return comma > 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    private void createFreePlanSubscription(User user) {
        Optional<SubscriptionPlan> freePlanOpt = subscriptionPlanRepository.findByCode("FREE");
        if (freePlanOpt.isPresent()) {
            SubscriptionPlan freePlan = freePlanOpt.get();
            Optional<UserSubscription> existingSubscription = userSubscriptionRepository
                    .findActiveSubscriptionByUserId(user.getId(), LocalDateTime.now());
            if (existingSubscription.isEmpty()) {
                UserSubscription subscription = new UserSubscription();
                subscription.setUser(user);
                subscription.setPlan(freePlan);
                subscription.setStatus("ACTIVE");
                subscription.setStartDate(LocalDateTime.now());
                subscription.setEndDate(null);
                subscription.setAutoRenew(false);
                userSubscriptionRepository.save(subscription);
                if (freePlan.getMaxDevices() != null && freePlan.getMaxDevices() > 0) {
                    user.setMaxDevices(freePlan.getMaxDevices());
                    userRepository.save(user);
                    System.out.println("[UserService] Updated maxDevices to " + freePlan.getMaxDevices()
                            + " from Free Plan for user: " + user.getEmail());
                }
                System.out.println("[UserService] Created Free Plan subscription for user: " + user.getEmail());
            } else {
                System.out.println(
                        "[UserService] User already has an active subscription, skipping Free Plan creation for: "
                                + user.getEmail());
            }
        } else {
            System.err.println("[UserService] WARNING: Free Plan with code 'FREE' not found in database! User "
                    + user.getEmail() + " was registered without a subscription.");
        }
    }
    private void createInactiveLXCoinsAccount(User user) {
        if (!lxCoinsAccountRepository.existsByUserId(user.getId())) {
            LXCoinsAccount account = new LXCoinsAccount();
            account.setUser(user);
            account.setBalance(java.math.BigDecimal.ZERO);
            account.setTotalEarned(java.math.BigDecimal.ZERO);
            account.setTotalSpent(java.math.BigDecimal.ZERO);
            account.setIsActive(false);
            lxCoinsAccountRepository.save(account);
        }
    }
}