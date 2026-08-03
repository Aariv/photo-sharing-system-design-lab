package com.ariv.photoshare.like.dto;

import java.util.UUID;

public record LikeResponse(
        UUID likeId,
        UUID userId,
        UUID postId
) {
}