package com.loyalixa.backend.user;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.UUID;
@Service
public class AvatarService {
    private static final List<String> DEFAULT_AVATARS = List.of(
        "https://api.dicebear.com/7.x/avataaars/svg?seed=",
        "https://api.dicebear.com/7.x/personas/svg?seed=",
        "https://api.dicebear.com/7.x/initials/svg?seed=",
        "https://api.dicebear.com/7.x/bottts/svg?seed=",
        "https://api.dicebear.com/7.x/identicon/svg?seed="
    );
    private final Random random = new Random();
    public String getRandomAvatar(UUID userId) {
        long seed = userId.getMostSignificantBits() ^ userId.getLeastSignificantBits();
        Random seededRandom = new Random(seed);
        String avatarBase = DEFAULT_AVATARS.get(seededRandom.nextInt(DEFAULT_AVATARS.size()));
        return avatarBase + userId.toString();
    }
    public String getRandomAvatar() {
        String avatarBase = DEFAULT_AVATARS.get(random.nextInt(DEFAULT_AVATARS.size()));
        return avatarBase + UUID.randomUUID().toString();
    }
    public String getAvatarUrl(String avatarUrl, UUID userId) {
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            return avatarUrl;
        }
        return getRandomAvatar(userId);
    }
}
