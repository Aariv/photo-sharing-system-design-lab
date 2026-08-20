package com.ariv.photoshare.outbox.service;

import com.ariv.photoshare.events.PostCreatedEvent;
import com.ariv.photoshare.events.PostCreatedEventPublisher;
import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class OutboxPublisher {

    private static final Logger LOG =
            Logger.getLogger(
                    OutboxPublisher.class);

    @Inject
    OutboxRepository repository;

    @Inject
    PostCreatedEventPublisher publisher;

    @Inject
    ObjectMapper objectMapper;

    @Scheduled(every = "5s")
    @Transactional
    void publishPendingEvents() {

        List<OutboxEventEntity> events =
                repository.findPending(100);

        for (OutboxEventEntity event : events) {

            try {

                PostCreatedEvent postEvent =
                        objectMapper.readValue(
                                event.payload,
                                PostCreatedEvent.class);

                publisher.publish(postEvent);

                repository.markProcessed(
                        event.id);

                LOG.infof(
                        "Processed outbox event=%s",
                        event.id);

            } catch (Exception exception) {

                repository.markFailed(
                        event.id,
                        exception.getMessage());

                LOG.errorf(
                        exception,
                        "Failed outbox event=%s",
                        event.id);
            }
        }
    }
}