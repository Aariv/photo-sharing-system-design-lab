package com.ariv.photoshare.follow.dto;

import java.util.UUID;

public record FollowRequest(
        UUID followerId
) {
}