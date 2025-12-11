package com.loyalixa.backend.security;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
@Service
public class DeviceFingerprintService {
    public String generateDeviceFingerprint(
            HttpServletRequest httpRequest,
            Integer screenWidth,
            Integer screenHeight,
            String timezone,
            String platform,
            Integer hardwareConcurrency,
            Double deviceMemory,
            Double devicePixelRatio,
            Boolean touchSupport
    ) {
        List<String> components = new ArrayList<>();
        String userAgent = httpRequest != null ? httpRequest.getHeader("User-Agent") : null;
        if (userAgent != null && !userAgent.trim().isEmpty()) {
            components.add("UA:" + normalizeUserAgent(userAgent));
        }
        String ipAddress = resolveClientIp(httpRequest);
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            String ipPrefix = getIpPrefix(ipAddress);
            components.add("IP:" + ipPrefix);
        }
        String acceptLanguage = httpRequest != null ? httpRequest.getHeader("Accept-Language") : null;
        if (acceptLanguage != null && !acceptLanguage.trim().isEmpty()) {
            String primaryLanguage = acceptLanguage.split(",")[0].split(";")[0].trim();
            components.add("LANG:" + primaryLanguage);
        }
        String acceptEncoding = httpRequest != null ? httpRequest.getHeader("Accept-Encoding") : null;
        if (acceptEncoding != null && !acceptEncoding.trim().isEmpty()) {
            components.add("ENC:" + acceptEncoding);
        }
        if (screenWidth != null && screenHeight != null) {
            String resolutionCategory = categorizeResolution(screenWidth, screenHeight);
            components.add("RES:" + resolutionCategory);
        }
        if (timezone != null && !timezone.trim().isEmpty()) {
            components.add("TZ:" + timezone);
        }
        if (platform != null && !platform.trim().isEmpty()) {
            components.add("PLAT:" + platform);
        }
        if (hardwareConcurrency != null && hardwareConcurrency > 0) {
            String cpuCategory = categorizeCPU(hardwareConcurrency);
            components.add("CPU:" + cpuCategory);
        }
        if (deviceMemory != null && deviceMemory > 0) {
            String ramCategory = categorizeRAM(deviceMemory);
            components.add("RAM:" + ramCategory);
        }
        if (devicePixelRatio != null && devicePixelRatio > 0) {
            String dprCategory = categorizeDPR(devicePixelRatio);
            components.add("DPR:" + dprCategory);
        }
        if (touchSupport != null) {
            components.add("TOUCH:" + touchSupport);
        }
        String dnt = httpRequest != null ? httpRequest.getHeader("DNT") : null;
        if (dnt != null) {
            components.add("DNT:" + dnt);
        }
        String connection = httpRequest != null ? httpRequest.getHeader("Connection") : null;
        if (connection != null) {
            components.add("CONN:" + connection);
        }
        String secChUa = httpRequest != null ? httpRequest.getHeader("Sec-CH-UA") : null;
        if (secChUa != null && !secChUa.trim().isEmpty()) {
            components.add("CHUA:" + normalizeUserAgent(secChUa));
        }
        String secChUaPlatform = httpRequest != null ? httpRequest.getHeader("Sec-CH-UA-Platform") : null;
        if (secChUaPlatform != null && !secChUaPlatform.trim().isEmpty()) {
            components.add("CHPLAT:" + secChUaPlatform);
        }
        String fingerprint = String.join("|", components);
        return hashFingerprint(fingerprint);
    }
    public String generateDeviceFingerprint(HttpServletRequest httpRequest) {
        return generateDeviceFingerprint(httpRequest, null, null, null, null, null, null, null, null);
    }
    public String generateDeviceFingerprint(
            HttpServletRequest httpRequest,
            Integer screenWidth,
            Integer screenHeight,
            String timezone,
            String platform
    ) {
        return generateDeviceFingerprint(httpRequest, screenWidth, screenHeight, timezone, platform, null, null, null, null);
    }
    private String normalizeUserAgent(String userAgent) {
        if (userAgent == null) return "";
        String normalized = userAgent
            .replaceAll("\\d+\\.\\d+\\.\\d+\\.\\d+", "XXX.XXX.XXX.XXX")
            .replaceAll("\\d+\\.\\d+\\.\\d+", "XXX.XXX.XXX")
            .replaceAll("\\d+\\.\\d+", "XXX.XXX")
            .replaceAll("\\d+", "X");
        return normalized;
    }
    private String resolveClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "CF-Connecting-IP"};
        for (String header : headers) {
            String value = request.getHeader(header);
            if (value != null && !value.trim().isEmpty()) {
                int comma = value.indexOf(',');
                return comma > 0 ? value.substring(0, comma).trim() : value.trim();
            }
        }
        return request.getRemoteAddr();
    }
    private String getIpPrefix(String ipAddress) {
        if (ipAddress == null) return "";
        if (ipAddress.contains(".")) {
            String[] parts = ipAddress.split("\\.");
            if (parts.length >= 3) {
                return parts[0] + "." + parts[1] + "." + parts[2];
            }
        }
        if (ipAddress.contains(":")) {
            String[] parts = ipAddress.split(":");
            if (parts.length >= 3) {
                return parts[0] + ":" + parts[1] + ":" + parts[2];
            }
        }
        return ipAddress;
    }
    private String categorizeResolution(int width, int height) {
        if (width >= 1920 && height >= 1080) return "FHD+";
        if (width >= 1366 && height >= 768) return "HD+";
        if (width >= 1280 && height >= 720) return "HD";
        if (width >= 1024 && height >= 768) return "TABLET";
        if (width >= 768 && height >= 1024) return "TABLET_PORT";
        if (width >= 414 && height >= 896) return "MOBILE_LARGE";
        if (width >= 375 && height >= 667) return "MOBILE";
        return "OTHER";
    }
    private String categorizeCPU(int cores) {
        if (cores <= 2) return "LOW";
        if (cores <= 4) return "MEDIUM";
        if (cores <= 8) return "HIGH";
        if (cores <= 16) return "VERY_HIGH";
        return "EXTREME";
    }
    private String categorizeRAM(double ramGB) {
        if (ramGB <= 2) return "LOW";
        if (ramGB <= 4) return "MEDIUM";
        if (ramGB <= 8) return "HIGH";
        if (ramGB <= 16) return "VERY_HIGH";
        return "EXTREME";
    }
    private String categorizeDPR(double dpr) {
        if (dpr <= 1.0) return "1X";
        if (dpr <= 1.5) return "1.5X";
        if (dpr <= 2.0) return "2X";
        if (dpr <= 2.5) return "2.5X";
        if (dpr <= 3.0) return "3X";
        return "3X+";
    }
    private String hashFingerprint(String fingerprint) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(fingerprint.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(fingerprint.hashCode());
        }
    }
}
