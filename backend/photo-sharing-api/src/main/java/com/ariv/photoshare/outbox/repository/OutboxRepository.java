package com.ariv.photoshare.outbox.repository;

import com.ariv.photoshare.outbox.entity.OutboxEventEntity;
import com.ariv.photoshare.outbox.entity.OutboxStatus;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class OutboxRepository
        implements PanacheRepositoryBase<OutboxEventEntity, UUID> {

    // Equivalent to:
    // SELECT * FROM outbox_events WHERE status = 'PENDING'
    // AND (next_attempt_at IS NULL OR next_attempt_at <= NOW()) ORDER BY created_at LIMIT :batchSize
    public List<OutboxEventEntity> findPending(int batchSize) {

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
}