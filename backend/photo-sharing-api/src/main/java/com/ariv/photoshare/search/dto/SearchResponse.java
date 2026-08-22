package com.ariv.photoshare.search.dto;

import java.time.Instant;
import java.util.UUID;

public record SearchResponse(
        UUID postId,
        UUID authorId,
        String caption,
        String imageUrl,
        Instant createdAt
) {
}