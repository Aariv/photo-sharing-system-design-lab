package com.ariv.photoshare.notifications.dto;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        String message,

        boolean isRead,

        Instant createdAt

) {
}