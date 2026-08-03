package com.ariv.photoshare.post.dto;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID userId,
        String imageUrl,
        String caption,
        Instant createdAt
) {
}