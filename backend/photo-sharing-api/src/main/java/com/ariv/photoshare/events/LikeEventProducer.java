package com.ariv.photoshare.events;

import com.ariv.photoshare.metrics.KafkaMetrics;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;

import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class LikeEventProducer {

    @Inject
    KafkaMetrics  metrics;

    @Channel("post-liked")
    Emitter<PostLikedEvent> emitter;

    public void publish(
            PostLikedEvent event) {

        emitter.send(event);
        metrics.eventPublished();
        System.out.println(
                "EVENT PUBLISHED -> " + event);
    }
}