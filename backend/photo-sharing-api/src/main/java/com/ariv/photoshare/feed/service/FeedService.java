package com.ariv.photoshare.feed.service;

import com.ariv.photoshare.cache.CacheKeys;
import com.ariv.photoshare.comment.repository.CommentRepository;
import com.ariv.photoshare.feed.dto.FeedItemResponse;
import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.like.repository.LikeRepositoryV1;
import com.ariv.photoshare.metrics.FeedMetrics;
import com.ariv.photoshare.post.repository.PostRepository;

import com.ariv.photoshare.upload.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

@ApplicationScoped
public class FeedService {

    @Inject
    PostRepository postRepository;

    @Inject
    LikeRepositoryV1 likeRepository;

    @Inject
    CommentRepository commentRepository;

    @Inject
    RedisDataSource redisDataSource;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    FeedMetrics metrics;

    @Inject
    FileStorageService storageService;

    @WithSpan("feed-service")
    public FeedResponse getFeed(
            UUID userId,
            int page,
            int size) {
        metrics.request();
        String cacheKey =
                CacheKeys.feed(userId);

        try {

            String cached =
                    cache().get(cacheKey);

            if (cached != null) {
                metrics.hit();

                System.out.println(
                        "CACHE HIT -> " + cacheKey);

                return objectMapper.readValue(
                        cached,
                        FeedResponse.class);
            }
            metrics.miss();
            System.out.println(
                    "CACHE MISS -> " + cacheKey);

        } catch (Exception e) {

            System.out.println(
                    "Redis read failed");
        }

        FeedResponse response =
                buildFeedFromDatabase(
                        userId,
                        page,
                        size);

        try {

            String json =
                    objectMapper.writeValueAsString(
                            response);

            cache().setex(
                    cacheKey,
                    60,
                    json);

        } catch (Exception e) {

            System.out.println(
                    "Redis write failed");
        }

        return response;
    }

    public FeedResponse buildFeedFromDatabase(
            UUID userId,
            int page,
            int size) {

        var items =
                postRepository
                        .findFeed(userId,page,size)
                        .stream()
                        .map(post -> {

                            long likesCount =
                                    likeRepository
                                            .countLikes(
                                                    post.id);

                            boolean likedByMe =
                                    likeRepository
                                            .likedByUser(
                                                    userId,
                                                    post.id);

                            long commentsCount =
                                    commentRepository
                                            .countComments(post.id);

                            String imageUrl = null;
                            try {
                                imageUrl = storageService
                                        .generatePresignedUrl(post.imageUrl);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            return new FeedItemResponse(

                                    post.id,

                                    post.userId,

                                    imageUrl,

                                    post.caption,

                                    post.createdAt,

                                    likesCount,

                                    commentsCount,

                                    likedByMe
                            );

                        })
                        .toList();

        return new FeedResponse(
                items,
                page,
                size
        );
    }

    @WithSpan("redis-cache-check")
    public ValueCommands<String, String> cache() {
        return redisDataSource.value(String.class);
    }

}