package com.ariv.photoshare.events;

import java.time.Instant;
import java.util.UUID;

public record PostUnlikedEvent(
        UUID eventId,
        UUID postId,
        UUID userId,
        Instant occurredAt
) {
}