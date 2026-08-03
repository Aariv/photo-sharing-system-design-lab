package com.ariv.photoshare.like.dto;

import java.util.UUID;

public record LikeRequest(
        UUID userId
) {
}