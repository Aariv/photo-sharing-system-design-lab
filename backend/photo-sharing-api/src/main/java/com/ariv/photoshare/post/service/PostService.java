package com.ariv.photoshare.post.service;

import com.ariv.photoshare.cache.service.CacheService;
import com.ariv.photoshare.events.PostCreatedEvent;
import com.ariv.photoshare.events.PostCreatedEventPublisher;
import com.ariv.photoshare.follow.entity.FollowEntity;
import com.ariv.photoshare.follow.repository.FollowRepository;
import com.ariv.photoshare.post.dto.CreatePostRequest;
import com.ariv.photoshare.post.dto.CreatePostResponse;
import com.ariv.photoshare.post.dto.PostResponse;
import com.ariv.photoshare.post.entity.PostEntity;
import com.ariv.photoshare.post.repository.PostRepository;
import com.ariv.photoshare.timeline.service.CelebrityService;
import com.ariv.photoshare.timeline.service.TimelineService;
import com.ariv.photoshare.upload.service.FileStorageService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class PostService {

    private static final Logger LOG = Logger.getLogger(PostService.class);

    @Inject
    private PostRepository repository;

    @Inject
    CacheService cacheService;

    @Inject
    FileStorageService storageService;

    @Inject
    FollowRepository followRepository;

    @Inject
    TimelineService timelineService;

    @Inject
    CelebrityService celebrityService;

    @Inject
    PostCreatedEventPublisher postCreatedEventPublisher;

    @Transactional
    @Deprecated
    public CreatePostResponse create1(
            CreatePostRequest request) {

        PostEntity post = new PostEntity();

        post.id = UUID.randomUUID();
        post.userId = request.userId();
        post.imageUrl = request.imageUrl();
        post.caption = request.caption();
        post.createdAt = Instant.now();

        repository.persist(post);

        boolean celebrity = celebrityService.isCelebrity(post.userId);
        // Normal User: Fan-out-on-write to all followers' timelines
        if(!celebrity) {
            List<FollowEntity> followers =
                    followRepository.findFollowersOf(post.userId);

            for (FollowEntity follower : followers) {

                timelineService.addEntry(
                        follower.followerId,
                        post.id,
                        post.userId,
                        post.createdAt
                );

                cacheService.evictFeed(follower.followerId);
            }
        } else {
            /*
            * Celebrity user:
            *
            * Do not fan out this post into follower timelines.
            * The post will be merged during feed read later.
            *
            * This prevents massive write amplification.
            */
            LOG.infof(
                    "Skipping fan-out-on-write for celebrity author=%s",
                    post.userId
            );
        }

        cacheService.evictFeed(request.userId());

        return new CreatePostResponse(post.id);
    }

    @Transactional
    public CreatePostResponse create(
            CreatePostRequest request) {

        PostEntity post = new PostEntity();

        post.id = UUID.randomUUID();
        post.userId = request.userId();
        post.imageUrl = request.imageUrl();
        post.caption = request.caption();
        post.createdAt = Instant.now();

        repository.persist(post);

        PostCreatedEvent event = new PostCreatedEvent(
                UUID.randomUUID(),
                post.id,
                post.userId,
                post.createdAt
        );

        postCreatedEventPublisher.publish(event);
        cacheService.evictFeed(request.userId());

        return new CreatePostResponse(post.id);
    }

    public PostResponse get(UUID postId) {

        PostEntity post =
                repository.findById(postId);

        if (post == null) {
            throw new NotFoundException();
        }

        String imageUrl = null;
        try {
            imageUrl = storageService
                    .generatePresignedUrl(post.imageUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new PostResponse(
                post.id,
                post.userId,
                imageUrl,
                post.caption,
                post.createdAt
        );
    }

}