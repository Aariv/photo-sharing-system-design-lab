package com.ariv.photoshare.comment.dto;

import java.util.UUID;

public record CreateCommentRequest(
        UUID userId,
        String comment
) {
}