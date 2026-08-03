package com.ariv.photoshare.user.dto;

import java.util.UUID;

public record SignupResponse(
        UUID id,
        String email,
        String password
) {
}