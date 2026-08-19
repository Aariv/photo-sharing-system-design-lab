package com.ariv.photoshare.timeline.dto;

import java.time.Instant;
import java.util.UUID;

public record TimelineResponse(
        UUID postId,
        UUID authorId,
        Instant createdAt
) {}