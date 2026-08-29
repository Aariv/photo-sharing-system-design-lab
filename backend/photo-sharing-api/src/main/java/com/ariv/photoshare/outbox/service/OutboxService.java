package com.ariv.photoshare.outbox.service;

import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.entity.OutboxStatus;
import com.ariv.photoshare.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.UUID;

@ApplicationScoped
public class OutboxService {

    @Inject
    OutboxRepository outboxRepository;

    @Inject
    ObjectMapper objectMapper;

    public void save(
            UUID eventId,
            String aggregateType,
            UUID aggregateId,
            String eventType,
            Object payload) {

        OutboxEventEntity entity =
                new OutboxEventEntity();

        entity.id = eventId;
        entity.aggregateType = aggregateType;
        entity.aggregateId = aggregateId;
        entity.eventType = eventType;
        entity.payload = serialize(payload);
        entity.status = OutboxStatus.PENDING;
        entity.attempts = 0;
        entity.createdAt = Instant.now();

        outboxRepository.persist(entity);
    }

    private String serialize(Object payload) {

        try {
            return objectMapper.writeValueAsString(
                    payload
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Unable to serialize outbox event",
                    exception
            );
        }
    }
}