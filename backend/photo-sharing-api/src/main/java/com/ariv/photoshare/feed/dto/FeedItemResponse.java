package com.ariv.photoshare.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedItemResponse(
        UUID postId,
        UUID userId,
        String imageUrl,
        String caption,
        Instant createdAt,
        long likesCount,
        long commentsCount,
        boolean likedByMe
) {
}