package com.ariv.photoshare.timeline.service;

import com.ariv.photoshare.comment.repository.CommentRepository;
import com.ariv.photoshare.feed.dto.FeedItemResponse;
import com.ariv.photoshare.feed.dto.FeedResponse;
import com.ariv.photoshare.like.repository.LikeRepository;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.upload.service.FileStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.UUID;

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

}