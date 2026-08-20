package com.ariv.photoshare.events;

import java.time.Instant;
import java.util.UUID;

public record PostCreatedEvent(
        UUID eventId,
        UUID postId,
        UUID authorId,
        Instant createdAt
) {
}