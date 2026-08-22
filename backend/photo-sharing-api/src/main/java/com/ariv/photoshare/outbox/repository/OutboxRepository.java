package com.ariv.photoshare.outbox.repository;

import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.entity.OutboxStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OutboxRepository
        implements PanacheRepositoryBase<OutboxEventEntity, UUID> {

    // Equivalent to:
    // SELECT * FROM outbox_events WHERE status = 'PENDING'
    // AND (next_attempt_at IS NULL OR next_attempt_at <= NOW()) ORDER BY created_at LIMIT :batchSize
    public List<OutboxEventEntity> findPending1(int batchSize) {

        return find(
                """
                status = ?1
                and (nextAttemptAt is null or nextAttemptAt <= ?2)
                order by createdAt
                """,
                OutboxStatus.PENDING,
                Instant.now()
        )
        .page(0, batchSize)
        .list();
    }

    // Equivalent to:
    // SELECT * FROM outbox_events WHERE status = 'PENDING'
    // AND (next_attempt_at IS NULL OR next_attempt_at <= NOW()) ORDER BY created_at LIMIT :batchSize
    // Functionality in plain english is
    // to find all pending outbox events that are ready to be processed, up to a specified batch size.
    // It filters events with a status of 'PENDING' and checks if the next attempt time is either null or has already passed
    // (i.e., less than or equal to the current time). The results are ordered by the creation time of the events and limited to the specified batch size.
    public List<OutboxEventEntity> findPending(
            int batchSize) {

        return find(
                """
                status = ?1
                and (
                        nextAttemptAt is null
                        or nextAttemptAt <= ?2
                    )
                order by createdAt
                """,
                OutboxStatus.PENDING,
                Instant.now()
        )
                .page(0, batchSize)
                .list();
    }

    // Equivalent to:
    // UPDATE outbox_events SET status = 'PROCESSED', processed_at = NOW()
    @Transactional
    public void markProcessed(UUID eventId) {

        update(
                "status = ?1, processedAt = ?2 where id = ?3",
                OutboxStatus.PROCESSED,
                Instant.now(),
                eventId
        );
    }

    // Equivalent to:
    // UPDATE outbox_events SET status = 'FAILED', attempts = attempts + 1, last_error = :error WHERE id = :eventId
    @Transactional
    public void markFailed(
            UUID eventId,
            String error) {

        update(
                """
                status = ?1,
                attempts = attempts + 1,
                lastError = ?2
                where id = ?3
                """,
                OutboxStatus.FAILED,
                error,
                eventId
        );
    }

    // Equivalent to:
    // UPDATE outbox_events SET status = 'PENDING', attempts = :currentAttempts,
    // last_error = :errorMessage, next_attempt_at = :nextRetryTime WHERE id = :eventId
    @Transactional
    public void scheduleRetry(
            UUID eventId,
            int currentAttempts,
            String errorMessage,
            Instant nextRetryTime) {

        update(
                """
                status = ?1,
                attempts = ?2,
                lastError = ?3,
                nextAttemptAt = ?4
                where id = ?5
                """,
                OutboxStatus.PENDING,
                currentAttempts,
                errorMessage,
                nextRetryTime,
                eventId
        );
    }

    // Equivalent to:
    // UPDATE outbox_events SET status = 'DEAD', attempts = :attempts,
    // last_error = :error WHERE id = :eventId
    @Transactional
    public void markDead(
            UUID eventId,
            int attempts,
            String error) {

        update(
                """
                status = ?1,
                attempts = ?2,
                lastError = ?3
                where id = ?4
                """,
                OutboxStatus.DEAD,
                attempts,
                error,
                eventId
        );
    }
}