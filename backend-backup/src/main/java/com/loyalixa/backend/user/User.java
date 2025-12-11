package com.loyalixa.backend.user;
import com.loyalixa.backend.course.CourseReview;
import com.loyalixa.backend.course.Enrollment;
import com.loyalixa.backend.course.LessonProgress;
import com.loyalixa.backend.marketing.GiftVoucher;
import com.loyalixa.backend.security.Role;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column
    private String passwordHash;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @Column(nullable = false, length = 20)
    private String status;
    @Column(nullable = false, length = 20)
    private String authProvider;
    @Column(name = "max_devices", nullable = false)
    private Integer maxDevices = 1;
    private LocalDateTime lastLoginAt;
    private LocalDateTime passwordChangedAt;
    private LocalDateTime deletedAt;
    private String deletionReason;
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;
    @Column(name = "suspend_reason", length = 1000)
    private String suspendReason;
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;
    @Column(name = "suspension_type", length = 20)
    private String suspensionType;
    @Column(name = "banned_at")
    private LocalDateTime bannedAt;
    @Column(name = "ban_reason", length = 1000)
    private String banReason;
    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;
    @Column(name = "ban_type", length = 20)
    private String banType;
    @Column(name = "appeal_requested")
    private Boolean appealRequested = false;
    @Column(name = "appeal_reason", length = 2000)
    private String appealReason;
    @Column(name = "appeal_requested_at")
    private LocalDateTime appealRequestedAt;
    @Column(name = "appeal_status", length = 20)
    private String appealStatus;
    @Column(name = "appeal_reviewed_at")
    private LocalDateTime appealReviewedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appeal_reviewed_by")
    private User appealReviewedBy;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    @Column(name = "user_agent", length = 512)
    private String userAgent;
    @Column(name = "browser", length = 100)
    private String browser;
    @Column(name = "operating_system", length = 100)
    private String operatingSystem;
    @Column(name = "device_type", length = 50)
    private String deviceType;
    @Column(name = "ip_address", length = 100)
    private String ipAddress;
    @Column(name = "accept_language", length = 255)
    private String acceptLanguage;
    @Column(name = "timezone", length = 100)
    private String timezone;
    @Column(name = "platform", length = 100)
    private String platform;
    @Column(name = "screen_width")
    private Integer screenWidth;
    @Column(name = "screen_height")
    private Integer screenHeight;
    @Column(name = "viewport_width")
    private Integer viewportWidth;
    @Column(name = "viewport_height")
    private Integer viewportHeight;
    @Column(name = "device_pixel_ratio")
    private Double devicePixelRatio;
    @Column(name = "hardware_concurrency")
    private Integer hardwareConcurrency;
    @Column(name = "device_memory_gb")
    private Double deviceMemoryGb;
    @Column(name = "touch_support")
    private Boolean touchSupport;
    @Column(name = "accept_encoding", length = 100)
    private String acceptEncoding;
    @Column(name = "dnt", length = 10)
    private String dnt;
    @Column(name = "referrer", length = 512)
    private String referrer;
    @Column(name = "host", length = 255)
    private String host;
    @Column(name = "origin", length = 255)
    private String origin;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private UserProfile userProfile;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private com.loyalixa.backend.staff.Staff staff;
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Enrollment> enrollments;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserActivityLog> activityLogs;
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<LessonProgress> lessonProgress;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserSocialAccount> socialAccounts;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<CourseReview> reviews;
    @OneToMany(mappedBy = "instructor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InstructorFollower> followers;
    @OneToMany(mappedBy = "follower", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<InstructorFollower> following;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GiftVoucher> sentGifts;
    @OneToMany(mappedBy = "redeemer", cascade = CascadeType.ALL)
    private Set<GiftVoucher> redeemedGifts;
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        if (this.role == null) {
            return authorities;
        }
        if (this.role.getPermissions() != null && !this.role.getPermissions().isEmpty()) {
            authorities.addAll(
                    this.role.getPermissions().stream()
                            .filter(permission -> permission != null && permission.getName() != null)
                            .map(permission -> {
                                return new SimpleGrantedAuthority(permission.getName());
                            })
                            .collect(Collectors.toSet()));
        } else {
            System.out.println("[User.getAuthorities] Permissions is NULL or EMPTY for role: "
                    + (this.role != null ? this.role.getName() : "NULL"));
        }
        String roleName = this.role != null ? this.role.getName() : null;
        if (roleName != null) {
            String roleAuthority = "ROLE_" + roleName.toUpperCase();
            authorities.add(new SimpleGrantedAuthority(roleAuthority));
        }
        return authorities;
    }
    @Override
    public String getPassword() {
        return this.passwordHash;
    }
    @Override
    public String getUsername() {
        return this.email;
    }
    public String getActualUsername() {
        return this.username;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return !this.status.equals("BLOCKED") &&
                !this.status.equals("SUSPENDED") &&
                !this.status.equals("BANNED") &&
                !this.status.equals("DELETED");
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return this.status.equals("ACTIVE");
    }
    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.loyalixa.backend.messaging.UserAlert> receivedAlerts;
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<com.loyalixa.backend.messaging.UserAlert> sentAlerts;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserSuspensionHistory> suspensionHistory;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<UserBanHistory> banHistory;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private com.loyalixa.backend.financial.FinancialAccount financialAccount;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<com.loyalixa.backend.subscription.UserSubscription> subscriptions;
    public Boolean getCanAccessDashboard() {
        if (this.staff != null) {
            return this.staff.getCanAccessDashboard() != null ? this.staff.getCanAccessDashboard() : false;
        }
        return false;
    }
    public void setCanAccessDashboard(Boolean canAccessDashboard) {
        if (this.role != null && !"STUDENT".equalsIgnoreCase(this.role.getName())) {
            if (this.staff == null) {
                this.staff = new com.loyalixa.backend.staff.Staff();
                this.staff.setUser(this);
            }
            this.staff.setCanAccessDashboard(canAccessDashboard != null ? canAccessDashboard : false);
        }
    }
}