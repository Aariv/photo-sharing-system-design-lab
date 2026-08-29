package com.ariv.photoshare.outbox.service;

import com.ariv.photoshare.events.PostCreatedEvent;
import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.entity.OutboxStatus;
import com.ariv.photoshare.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;

@ApplicationScoped
public class OutboxServiceV1 {

    private static final String POST_AGGREGATE = "POST";
    private static final String POST_CREATED_EVENT = "POST_CREATED";

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    public void savePostCreatedEvent(PostCreatedEvent event) {

        OutboxEventEntity outboxEvent = new OutboxEventEntity();

        outboxEvent.id = event.eventId();
        outboxEvent.aggregateType = POST_AGGREGATE;
        outboxEvent.aggregateId = event.postId();
        outboxEvent.eventType = POST_CREATED_EVENT;
        outboxEvent.payload = serialize(event);
        outboxEvent.status = OutboxStatus.PENDING;
        outboxEvent.attempts = 0;
        outboxEvent.createdAt = Instant.now();

        outboxRepository.persist(outboxEvent);
    }

    private String serialize(PostCreatedEvent event) {

        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize PostCreatedEvent",
                    exception
            );
        }
    }
}