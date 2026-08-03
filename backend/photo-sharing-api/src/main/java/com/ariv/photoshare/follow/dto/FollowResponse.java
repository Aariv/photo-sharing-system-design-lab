package com.ariv.photoshare.follow.dto;

import java.util.UUID;

public record FollowResponse(
        UUID id,
        UUID followerId,
        UUID followingId
) {
}