package com.ariv.photoshare.common.exception;

import java.time.Instant;

public record ApiError(
        String errorCode,
        String message,
        Instant timestamp
) {}