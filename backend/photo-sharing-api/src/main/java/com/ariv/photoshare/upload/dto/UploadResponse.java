package com.ariv.photoshare.upload.dto;

public record UploadResponse(
        String imageUrl,
        String objectName
) {
}