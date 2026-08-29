package com.ariv.photoshare.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PostLikedEventPublisher {

    private static final Logger LOG =
            Logger.getLogger(PostLikedEventPublisher.class);

    @Inject
    @Channel("post-liked")
    Emitter<PostLikedEvent> emitter;

    public CompletionStage<Void> publish(
            PostLikedEvent event) {

        return emitter.send(event);
    }
}