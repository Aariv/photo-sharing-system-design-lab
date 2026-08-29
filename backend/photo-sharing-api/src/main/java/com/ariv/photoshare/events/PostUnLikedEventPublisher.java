package com.ariv.photoshare.events;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.concurrent.CompletionStage;

@ApplicationScoped
public class PostUnLikedEventPublisher {

    private static final Logger LOG =
            Logger.getLogger(PostUnLikedEventPublisher.class);

    @Inject
    @Channel("post-unliked")
    Emitter<PostUnlikedEvent> emitter;

    public CompletionStage<Void> publish(
            PostUnlikedEvent event) {

        return emitter.send(event);
    }
}