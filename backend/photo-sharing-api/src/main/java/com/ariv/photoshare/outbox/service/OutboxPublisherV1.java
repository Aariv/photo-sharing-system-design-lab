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

import java.time.Instant;
import java.util.List;

@ApplicationScoped
public class OutboxPublisherV1 {

    private static final Logger LOG =
            Logger.getLogger(
                    OutboxPublisherV1.class);

    private static final int MAX_RETRIES = 10;

    private static final long BASE_DELAY_SECONDS = 30;

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

//        for (OutboxEventEntity event : events) {
//
//            try {
//
//                PostCreatedEvent postEvent =
//                        objectMapper.readValue(
//                                event.payload,
//                                PostCreatedEvent.class);
//
//                publisher.publish(postEvent);
//
//                repository.markProcessed(
//                        event.id);
//
//                LOG.infof(
//                        "Processed outbox event=%s",
//                        event.id);
//
//            } catch (Exception exception) {
//
//                repository.markFailed(
//                        event.id,
//                        exception.getMessage());
//
//                LOG.errorf(
//                        exception,
//                        "Failed outbox event=%s",
//                        event.id);
//            }
//        }

        for (OutboxEventEntity event : events) {

            try {

                PostCreatedEvent postEvent =
                        objectMapper.readValue(
                                event.payload,
                                PostCreatedEvent.class);

//                publisher.publish(postEvent);

                publisher.publish(postEvent)
                        .toCompletableFuture()
                        .join();

                repository.markProcessed(event.id);

                LOG.infof(
                        "Processed event=%s",
                        event.id);

            } catch (Exception exception) {

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

                    continue;
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
        }
    }

    private long calculateRetryDelay(
            int attempts) {

        long delay =
                (long) Math.pow(2, attempts - 1)
                        * BASE_DELAY_SECONDS;

        return Math.min(delay, 3600);
    }
}