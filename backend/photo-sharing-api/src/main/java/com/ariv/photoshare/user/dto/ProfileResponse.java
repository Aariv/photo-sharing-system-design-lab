package com.ariv.photoshare.user.dto;

import java.util.UUID;

public record ProfileResponse(

        UUID userId,

        String username,

        String email,

        long postsCount,

        long followersCount,

        long followingCount

) {
}