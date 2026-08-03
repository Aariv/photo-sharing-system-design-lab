package com.ariv.photoshare.cache;

import java.util.UUID;

public final class CacheKeys {

    private CacheKeys() {
    }

    public static String feed(UUID userId) {
        return "feed:user:" + userId;
    }
}