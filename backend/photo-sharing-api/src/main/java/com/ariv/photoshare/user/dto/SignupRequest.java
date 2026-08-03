package com.ariv.photoshare.user.dto;

public record SignupRequest(
        String username,
        String email,
        String password
) {
}