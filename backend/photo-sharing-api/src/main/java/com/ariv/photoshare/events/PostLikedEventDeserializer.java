package com.ariv.photoshare.events;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PostLikedEventDeserializer
        extends ObjectMapperDeserializer<PostLikedEvent> {

    public PostLikedEventDeserializer() {
        super(PostLikedEvent.class);
    }
}