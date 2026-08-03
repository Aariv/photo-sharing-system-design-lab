package com.ariv.photoshare.notifications.dto;

import java.util.UUID;

public record MarkReadResponse(

        UUID id,

        boolean read

) {
}