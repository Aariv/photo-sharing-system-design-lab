package com.ariv.photoshare.timeline.service;

import com.ariv.photoshare.follow.repository.FollowRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.UUID;

@ApplicationScoped
public class CelebrityService {

    @Inject
    FollowRepository followRepository;

    @ConfigProperty(name = "feed.celebrity-threshold", defaultValue = "10000")
    long celebrityThreshold;

    public boolean isCelebrity(UUID userId) {

        long followerCount = followRepository.countFollowers(userId);

        return followerCount >= celebrityThreshold;
    }
}