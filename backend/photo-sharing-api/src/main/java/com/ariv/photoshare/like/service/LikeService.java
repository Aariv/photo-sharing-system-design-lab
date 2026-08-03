package com.ariv.photoshare.like.service;

import com.ariv.photoshare.cache.service.CacheService;
import com.ariv.photoshare.like.dto.LikeResponse;
import com.ariv.photoshare.like.entity.LikeEntity;
import com.ariv.photoshare.like.repository.LikeRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class LikeService {

    @Inject
    LikeRepository repository;

    @Inject
    CacheService cacheService;

    @Transactional
    public LikeResponse like(
            UUID userId,
            UUID postId) {

        if (repository.exists(userId, postId)) {
            throw new BadRequestException(
                    "Post already liked");
        }

        LikeEntity entity = new LikeEntity();

        entity.id = UUID.randomUUID();
        entity.userId = userId;
        entity.postId = postId;
        entity.createdAt = Instant.now();

        repository.persist(entity);

        cacheService.evictFeed(userId);

        return new LikeResponse(
                entity.id,
                entity.userId,
                entity.postId
        );
    }

    @Transactional
    public void unlike(
            UUID userId,
            UUID postId) {

        LikeEntity entity =
                repository.findLike(
                        userId,
                        postId);

        if (entity == null) {
            throw new NotFoundException();
        }

        repository.delete(entity);
        // Cache Invalidation
        cacheService.evictFeed(userId);
    }

    public long countLikes(UUID postId) {

        return repository.countLikes(postId);
    }
}