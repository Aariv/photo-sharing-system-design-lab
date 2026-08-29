package com.ariv.photoshare.outbox.service;

import com.ariv.photoshare.events.*;
import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class OutboxPublisher {

    private static final Logger LOG =
            Logger.getLogger(
                    OutboxPublisher.class);

    private static final int MAX_RETRIES = 10;

    private static final long BASE_DELAY_SECONDS = 30;

    @Inject
    OutboxRepository repository;

    @Inject
    PostCreatedEventPublisher publisher;

    @Inject
    PostLikedEventPublisher postLikedEventPublisher;

    @Inject
    PostUnLikedEventPublisher postUnLikedEventPublisher;

    @Inject
    ObjectMapper objectMapper;

    @Scheduled(every = "5s")
    @Transactional
    void publishPendingEvents() {

        List<OutboxEventEntity> events = repository.findPending(100);

        for (OutboxEventEntity outboxEvent : events) {
            switch (outboxEvent.eventType) {

                case "POST_CREATED" ->
                        publishPostCreated(outboxEvent);

                case "POST_LIKED" ->
                        publishPostLiked(outboxEvent);

                case "POST_UNLIKED" ->
                        publishPostUnliked(outboxEvent);

                default ->
                        throw new IllegalArgumentException(
                                "Unsupported event type: "
                                        + outboxEvent.eventType
                        );
            }
        }
    }

    private void publishPostUnliked(OutboxEventEntity event) {
        publishEvent(
                event,
                PostUnlikedEvent.class,
                postEvent -> postUnLikedEventPublisher
                        .publish(postEvent)
                        .toCompletableFuture()
                        .join());
    }

    private void publishPostLiked(OutboxEventEntity event) {
        publishEvent(
                event,
                PostLikedEvent.class,
                postEvent -> postLikedEventPublisher
                        .publish(postEvent)
                        .toCompletableFuture()
                        .join());
    }

    public void publishPostCreated(OutboxEventEntity event) {
        publishEvent(
                event,
                PostCreatedEvent.class,
                postEvent -> publisher
                        .publish(postEvent)
                        .toCompletableFuture()
                        .join());
    }

    private <T> void publishEvent(
            OutboxEventEntity event,
            Class<T> payloadType,
            EventPublisher<T> publishAction) {
        try {

            T postEvent =
                    objectMapper.readValue(
                            event.payload,
                            payloadType);

            publishAction.publish(postEvent);

            repository.markProcessed(event.id);

            LOG.infof(
                    "Processed event=%s",
                    event.id);

        } catch (Exception exception) {
            handlePublishFailure(event, exception);
        }
    }

    private void handlePublishFailure(
            OutboxEventEntity event,
            Exception exception) {

        int nextAttempt =
                event.attempts + 1;

        if (nextAttempt > MAX_RETRIES) {

            repository.markDead(
                    event.id,
                    nextAttempt,
                    exception.getMessage());

            LOG.errorf(
                    exception,
                    "Event moved to DEAD queue id=%s",
                    event.id);

            return;
        }

        long delaySeconds =
                calculateRetryDelay(
                        nextAttempt);

        Instant retryAt = Instant.now().plusSeconds(delaySeconds);

        repository.scheduleRetry(
                event.id,
                nextAttempt,
                exception.getMessage(),
                retryAt);

        LOG.warnf(
                exception,
                "Retry scheduled id=%s attempt=%d retryAt=%s",
                event.id,
                nextAttempt,
                retryAt);
    }

    @FunctionalInterface
    private interface EventPublisher<T> {
        void publish(T event) throws Exception;
    }

    private long calculateRetryDelay(
            int attempts) {

        long delay =
                (long) Math.pow(2, attempts - 1)
                        * BASE_DELAY_SECONDS;

        return Math.min(delay, 3600);
    }
}