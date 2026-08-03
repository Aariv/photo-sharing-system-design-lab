package com.ariv.photoshare.follow.service;

import com.ariv.photoshare.cache.service.CacheService;
import com.ariv.photoshare.follow.dto.FollowResponse;
import com.ariv.photoshare.follow.entity.FollowEntity;
import com.ariv.photoshare.follow.repository.FollowRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class FollowService {

    @Inject
    FollowRepository repository;

    @Inject
    CacheService cacheService;

    @Transactional
    public FollowResponse follow(
            UUID followerId,
            UUID followingId) {

        if (followerId.equals(followingId)) {
            throw new BadRequestException(
                    "Cannot follow yourself");
        }

        if (repository.exists(
                followerId,
                followingId)) {

            throw new BadRequestException(
                    "Already following user");
        }

        FollowEntity entity = new FollowEntity();

        entity.id = UUID.randomUUID();
        entity.followerId = followerId;
        entity.followingId = followingId;
        entity.createdAt = Instant.now();

        repository.persist(entity);

        cacheService.evictFeed(followerId);

        return new FollowResponse(
                entity.id,
                entity.followerId,
                entity.followingId
        );
    }

    @Transactional
    public void unfollow(
            UUID followerId,
            UUID followingId) {

        FollowEntity entity =
                repository.findFollow(
                        followerId,
                        followingId);

        if (entity == null) {
            throw new NotFoundException(
                    "Follow relationship not found");
        }

        repository.delete(entity);
    }
}