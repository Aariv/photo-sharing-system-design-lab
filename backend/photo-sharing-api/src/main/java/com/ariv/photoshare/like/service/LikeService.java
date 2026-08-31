package com.ariv.photoshare.like.service;

import com.ariv.photoshare.events.PostLikedEvent;
import com.ariv.photoshare.events.PostUnlikedEvent;
import com.ariv.photoshare.like.dto.LikeCommandResponse;
import com.ariv.photoshare.like.repository.LikeRepository;
import com.ariv.photoshare.outbox.service.OutboxService;
import com.ariv.photoshare.post.repository.PostRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class LikeService {

    public static final String AGGREGATE_TYPE = "POST";
    public static final String POST_LIKED = "POST_LIKED";
    public static final String POST_UNLIKED = "POST_UNLIKED";

    @Inject
    LikeRepository repository;

    @Inject
    OutboxService outboxService;

    @Inject
    PostRepository postRepository;

    @Transactional
    public LikeCommandResponse like(
            UUID userId,
            UUID postId) {

        UUID likeId = UUID.randomUUID();

        boolean created =
                repository.insertIfAbsent(
                        likeId,
                        userId,
                        postId
                );

        /*
         * Only create the outbox event if this request
         * actually created the authoritative like row.
         *
         * Duplicate API retries must not create
         * duplicate PostLikedEvent records.
         */
        if (created) {

            postRepository.incrementLikeCount(postId);

            // Outbox event

            UUID eventId = UUID.randomUUID();
            Instant occurredAt = Instant.now();

            PostLikedEvent event =
                    new PostLikedEvent(
                            eventId,
                            postId,
                            userId,
                            occurredAt
                    );

            outboxService.save(
                    eventId,
                    AGGREGATE_TYPE,
                    postId,
                    POST_LIKED,
                    event
            );
        }

        return new LikeCommandResponse(
                postId,
                userId,
                created
        );
    }

    @Transactional
    public void unlike(
            UUID userId,
            UUID postId) {

        boolean deleted =
                repository.deleteIfPresent(
                        userId,
                        postId
                );

        /*
         * DELETE is idempotent.
         * If the row already does not exist,
         * the requested final state is satisfied.
         */
        if (!deleted) {
            return;
        }

        postRepository.decrementLikeCount(postId);

        // Outbox Event

        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        PostUnlikedEvent event =
                new PostUnlikedEvent(
                        eventId,
                        postId,
                        userId,
                        occurredAt
                );

        outboxService.save(
                eventId,
                AGGREGATE_TYPE,
                postId,
                POST_UNLIKED,
                event
        );
    }

    public long countLikes(UUID postId) {
        return postRepository.getLikeCount(postId);
    }

    public boolean likedByUser(
            UUID userId,
            UUID postId) {

        return repository.likedByUser(
                userId,
                postId
        );
    }
}