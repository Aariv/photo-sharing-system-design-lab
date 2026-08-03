package com.ariv.photoshare.events;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.reactive.messaging.Channel;

import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class LikeEventProducer {

    @Channel("post-liked")
    Emitter<PostLikedEvent> emitter;

    public void publish(
            PostLikedEvent event) {

        emitter.send(event);

        System.out.println(
                "EVENT PUBLISHED -> " + event);
    }
}