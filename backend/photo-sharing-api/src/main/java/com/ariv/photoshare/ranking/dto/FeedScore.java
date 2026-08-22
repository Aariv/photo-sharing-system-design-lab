package com.ariv.photoshare.ranking.dto;

import com.ariv.photoshare.timeline.dto.TimelineFeedResponse;

public record FeedScore(
        TimelineFeedResponse post,
        double score
) {}