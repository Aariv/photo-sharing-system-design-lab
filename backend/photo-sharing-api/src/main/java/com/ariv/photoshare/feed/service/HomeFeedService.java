package com.ariv.photoshare.feed.service;

import com.ariv.photoshare.feed.dto.HomeFeedRow;
import com.ariv.photoshare.feed.repository.HomeFeedReadRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HomeFeedService {

    private static final int DEFAULT_LIMIT = 50;

    @Inject
    HomeFeedReadRepository readRepository;

    public List<HomeFeedRow> getHomeFeed(
            UUID userId) {

        return readRepository.findFeed(
                userId,
                DEFAULT_LIMIT
        );
    }
}