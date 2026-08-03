package com.ariv.photoshare.events;

import java.time.Instant;
import java.util.UUID;

public record PostLikedEvent(

        UUID postId,

        UUID userId,

        Instant createdAt

) {
}