package com.ariv.photoshare.feed.dto;

import java.util.List;

public record FeedResponse(
        List<FeedItemResponse> items,
        int page,
        int size
) {
}