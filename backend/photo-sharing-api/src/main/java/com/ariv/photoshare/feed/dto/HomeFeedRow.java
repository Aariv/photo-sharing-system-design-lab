package com.ariv.photoshare.feed.dto;

import java.time.Instant;
import java.util.UUID;

public record HomeFeedRow(
        UUID postId,
        UUID authorId,
        String caption,
        String imageUrl,
        Instant createdAt,
        long likeCount,
        long commentCount
) {
}