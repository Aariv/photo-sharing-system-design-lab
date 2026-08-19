package com.ariv.photoshare.timeline.dto;

import java.time.Instant;
import java.util.UUID;

public record TimelineFeedResponse(
        UUID postId,
        UUID authorId,
        String caption,
        String imageUrl,
        Instant createdAt
) {
}