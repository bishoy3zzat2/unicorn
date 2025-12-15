package com.unicorn.backend.user;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Service for generating and managing user avatars using DiceBear API.
 */
@Service
public class AvatarService {

    private static final List<String> DEFAULT_AVATARS = List.of(
            "https://api.dicebear.com/7.x/avataaars/svg?seed=",
            "https://api.dicebear.com/7.x/personas/svg?seed="
            // "https://api.dicebear.com/7.x/initials/svg?seed=",
            // "https://api.dicebear.com/7.x/bottts/svg?seed=",
            // "https://api.dicebear.com/7.x/identicon/svg?seed="
        );

    private final Random random = new Random();

    /**
     * Generate a deterministic avatar URL based on user ID.
     * Same user ID will always get the same avatar style and seed.
     *
     * @param userId the user's unique identifier
     * @return avatar URL from DiceBear API
     */
    public String getRandomAvatar(UUID userId) {
        long seed = userId.getMostSignificantBits() ^ userId.getLeastSignificantBits();
        Random seededRandom = new Random(seed);
        String avatarBase = DEFAULT_AVATARS.get(seededRandom.nextInt(DEFAULT_AVATARS.size()));
        return avatarBase + userId.toString();
    }

    /**
     * Generate a random avatar URL (non-deterministic).
     *
     * @return random avatar URL from DiceBear API
     */
    public String getRandomAvatar() {
        String avatarBase = DEFAULT_AVATARS.get(random.nextInt(DEFAULT_AVATARS.size()));
        return avatarBase + UUID.randomUUID().toString();
    }

    /**
     * Get avatar URL - returns custom avatar if provided, otherwise generates
     * default.
     *
     * @param avatarUrl custom avatar URL (nullable)
     * @param userId    user ID for generating default avatar
     * @return avatar URL (custom or default)
     */
    public String getAvatarUrl(String avatarUrl, UUID userId) {
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            return avatarUrl;
        }
        return getRandomAvatar(userId);
    }
}
