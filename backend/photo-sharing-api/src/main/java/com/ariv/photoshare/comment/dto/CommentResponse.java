package com.ariv.photoshare.comment.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID userId,
        UUID postId,
        String comment,
        Instant createdAt
) {
}