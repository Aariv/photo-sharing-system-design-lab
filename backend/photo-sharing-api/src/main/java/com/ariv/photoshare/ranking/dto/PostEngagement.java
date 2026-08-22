package com.ariv.photoshare.ranking.dto;

import java.util.UUID;

public record PostEngagement(
        UUID postId,
        long count
) {
}