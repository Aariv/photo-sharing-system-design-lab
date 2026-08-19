package com.ariv.photoshare.timeline.service;

import com.ariv.photoshare.comment.repository.CommentRepository;
import com.ariv.photoshare.feed.dto.FeedItemResponse;
import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.like.repository.LikeRepository;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.timeline.dto.TimelineFeedResponse;
import com.ariv.photoshare.timeline.entity.TimelineEntry;
import com.ariv.photoshare.timeline.entity.TimelineId;
import com.ariv.photoshare.timeline.repository.TimelineRepository;
import com.ariv.photoshare.upload.service.FileStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class TimelineService {

    @Inject
    PostRepository postRepository;

    @Inject
    LikeRepository likeRepository;

    @Inject
    CommentRepository commentRepository;

    @Inject
    FileStorageService storageService;

    @Inject
    TimelineRepository timelineRepository;

    public FeedResponse getTimeline(
            UUID userId,
            int page,
            int size) {

        var items =
                postRepository
                        .findTimeline(
                                userId,
                                page,
                                size)
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

    public List<TimelineEntry> getTimeline(UUID userId) {
        return timelineRepository.findByUserId(userId);
    }

    public void addEntry(
            UUID userId,
            UUID postId,
            UUID authorId,
            Instant createdAt) {

        TimelineEntry entry =
                new TimelineEntry(
                        new TimelineId(userId, postId),
                        authorId,
                        createdAt);

        timelineRepository.persist(entry);
    }

    public List<TimelineFeedResponse> getFeed(UUID userId) {

        List<TimelineEntry> entries =
                timelineRepository.findByUserId(userId);

        if (entries.isEmpty()) {
            return List.of();
        }

        List<UUID> postIds =
                entries.stream()
                        .map(entry ->
                                entry.getId().getPostId())
                        .toList();

        List<PostEntity> posts =
                postRepository.findByIds(postIds);

        Map<UUID, PostEntity> postMap =
                posts.stream()
                        .collect(Collectors.toMap(
                                p -> p.id,
                                p -> p
                        ));

        return entries.stream()
                .map(entry -> {

                    PostEntity post =
                            postMap.get(
                                    entry.getId().getPostId());

                    if (post == null) {
                        return null;
                    }

                    return new TimelineFeedResponse(
                            post.id,
                            post.userId,
                            post.caption,
                            post.imageUrl,
                            post.createdAt
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}