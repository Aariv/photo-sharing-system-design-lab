package com.ariv.photoshare.like.dto;

import java.util.UUID;

public record LikeCommandResponse(
        UUID postId,
        UUID userId,
        boolean created
) {
}