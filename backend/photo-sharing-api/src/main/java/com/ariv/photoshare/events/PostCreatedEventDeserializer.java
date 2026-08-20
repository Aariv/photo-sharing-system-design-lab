package com.ariv.photoshare.events;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class PostCreatedEventDeserializer
        extends ObjectMapperDeserializer<PostCreatedEvent> {

    public PostCreatedEventDeserializer() {
        super(PostCreatedEvent.class);
    }
}