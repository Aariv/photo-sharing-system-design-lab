package com.ariv.photoshare.timeline.service;

import com.ariv.photoshare.comment.repository.CommentRepository;
import com.ariv.photoshare.feed.dto.FeedItemResponse;
import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.follow.repository.FollowRepository;
import com.ariv.photoshare.like.repository.LikeRepositoryV1;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.ranking.dto.FeedScore;
import com.ariv.photoshare.ranking.service.RankingService;
import com.ariv.photoshare.timeline.dto.TimelineFeedResponse;
import com.ariv.photoshare.timeline.entity.TimelineEntry;
import com.ariv.photoshare.timeline.entity.TimelineId;
import com.ariv.photoshare.timeline.repository.TimelineRepository;
import com.ariv.photoshare.upload.service.FileStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class TimelineService {

    @Inject
    PostRepository postRepository;

    @Inject
    LikeRepositoryV1 likeRepository;

    @Inject
    CommentRepository commentRepository;

    @Inject
    FileStorageService storageService;

    @Inject
    TimelineRepository timelineRepository;

    @Inject
    CelebrityService celebrityService;

    @Inject
    FollowRepository followRepository;

    @Inject
    RankingService rankingService;

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

        TimelineId timelineId = new TimelineId(userId, postId);

        if(timelineRepository.findById(timelineId) != null) {
            return;
        }


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

        var timelineFeed =  entries.stream()
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

        List<UUID> celebrityAuthors =
                celebrityAuthorsFollowedBy(userId);

        List<PostEntity> celebrityPosts =
                postRepository.findPostsByAuthors(
                        celebrityAuthors);

        List<TimelineFeedResponse> celebrityFeed =
                celebrityPosts.stream()
                        .map(post ->
                                new TimelineFeedResponse(
                                        post.id,
                                        post.userId,
                                        post.caption,
                                        post.imageUrl,
                                        post.createdAt
                                ))
                        .toList();

//        return Stream.concat(
//                        timelineFeed.stream(),
//                        celebrityFeed.stream()
//                )
//                .sorted(
//                        Comparator.comparing(
//                                TimelineFeedResponse::createdAt
//                        ).reversed()
//                )
//                .limit(50)
//                .toList();

        List<TimelineFeedResponse> mergedFeed =
                Stream.concat(
                        timelineFeed.stream(),
                        celebrityFeed.stream()
                ).toList();

        List<UUID> mPostIds =
                mergedFeed.stream()
                        .map(TimelineFeedResponse::postId)
                        .toList();

        Map<UUID, Long> likeCounts =
                likeRepository.countByPostIds(
                        mPostIds);

        Map<UUID, Long> commentCounts =
                commentRepository.countByPostIds(
                        mPostIds);

        List<FeedScore> scoredFeed =
                mergedFeed.stream()
                        .map(post -> {

                            long likes =
                                    likeCounts.getOrDefault(
                                            post.postId(),
                                            0L
                                    );

                            long comments =
                                    commentCounts.getOrDefault(
                                            post.postId(),
                                            0L
                                    );

                            double score =
                                    rankingService.calculateScore(
                                            post.createdAt(),
                                            likes,
                                            comments
                                    );

                            return new FeedScore(
                                    post,
                                    score
                            );
                        })
                        .toList();

        return scoredFeed.stream()
                .sorted(
                        Comparator.comparing(
                                FeedScore::score
                        ).reversed()
                )
                .map(FeedScore::post)
                .limit(50)
                .toList();
    }

    private List<UUID> celebrityAuthorsFollowedBy(
            UUID userId) {

        return followRepository
                .findFollowingIds(userId)
                .stream()
                .filter(celebrityService::isCelebrity)
                .toList();
    }
}