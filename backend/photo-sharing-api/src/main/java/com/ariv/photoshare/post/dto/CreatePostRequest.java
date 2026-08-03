package com.ariv.photoshare.post.dto;

import java.util.UUID;

public record CreatePostRequest(
        UUID userId,
        String imageUrl,
        String caption
) {
}