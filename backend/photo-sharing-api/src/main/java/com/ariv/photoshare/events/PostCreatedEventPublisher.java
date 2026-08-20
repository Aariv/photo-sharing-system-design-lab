package com.ariv.photoshare.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PostCreatedEventPublisher {

    private static final Logger LOG =
            Logger.getLogger(PostCreatedEventPublisher.class);

    @Inject
    @Channel("post-created")
    Emitter<PostCreatedEvent> emitter;

    public void publish(PostCreatedEvent event) {

        emitter.send(event);

        LOG.infof(
                "Published PostCreatedEvent eventId=%s postId=%s authorId=%s",
                event.eventId(),
                event.postId(),
                event.authorId()
        );
    }
}