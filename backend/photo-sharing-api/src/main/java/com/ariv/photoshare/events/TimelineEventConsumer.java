package com.ariv.photoshare.events;

import com.ariv.photoshare.cache.service.CacheService;
import com.ariv.photoshare.follow.entity.FollowEntity;
import com.ariv.photoshare.follow.repository.FollowRepository;
import com.ariv.photoshare.timeline.service.CelebrityService;
import com.ariv.photoshare.timeline.service.TimelineService;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class TimelineEventConsumer {

    private static final Logger LOG =
            Logger.getLogger(TimelineEventConsumer.class);

    @Inject
    CelebrityService celebrityService;

    @Inject
    FollowRepository followRepository;

    @Inject
    TimelineService timelineService;

    @Inject
    CacheService cacheService;

    @Incoming("post-created-in")
    @Blocking
    @Transactional
    public void consume(PostCreatedEvent event) {

        LOG.infof(
                "Received PostCreatedEvent eventId=%s postId=%s authorId=%s",
                event.eventId(),
                event.postId(),
                event.authorId()
        );

        if (celebrityService.isCelebrity(event.authorId())) {

            LOG.infof(
                    "Skipping timeline fan-out for celebrity authorId=%s postId=%s",
                    event.authorId(),
                    event.postId()
            );

            return;
        }

        List<FollowEntity> followers =
                followRepository.findFollowersOf(event.authorId());

        for (FollowEntity follower : followers) {

            timelineService.addEntry(
                    follower.followerId,
                    event.postId(),
                    event.authorId(),
                    event.createdAt()
            );

            cacheService.evictFeed(follower.followerId);
        }

        LOG.infof(
                "Timeline fan-out completed eventId=%s postId=%s followers=%d",
                event.eventId(),
                event.postId(),
                followers.size()
        );
    }
}